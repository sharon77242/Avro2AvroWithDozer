package org.mashov

import com.github.dozermapper.core.metadata.ClassMappingMetadata
import com.github.dozermapper.core.metadata.FieldMappingMetadata
import mu.KotlinLogging
import org.apache.avro.Schema
import org.apache.avro.specific.SpecificRecord
import org.mashov.AvroUtils.getUnionSchema
import org.mashov.AvroUtils.primitive
import org.mashov.AvroUtils.required
import java.util.function.Consumer
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

private val logger = KotlinLogging.logger {}
class RequiredFieldsValidator(private val classMappingMetadata: ClassMappingMetadata) {
    private val requiredFieldsOnSchema: MutableList<FieldMappingMetadata> = ArrayList()

    private fun requiredFieldMissing(field: String): Boolean {
        return try {
            classMappingMetadata.getFieldMappingByDestination(field)
            false
        } catch (e: Exception) {
            true
        }
    }

    private fun unionHasInnerRequiredFieldsOnMapping(fieldName: String): Boolean {
        return classMappingMetadata.fieldMappings.any { it.destinationName.contains(fieldName) }
    }

    private fun validateNonPrimitive(field: Schema.Field, fieldName: String) {
        var fieldSchema = field.schema()
        if (fieldSchema.isUnion) {
            if (requiredFieldMissing(fieldName) && !unionHasInnerRequiredFieldsOnMapping(fieldName)) {
                return
            }

            fieldSchema = fieldSchema.getUnionSchema()
        }

        if (fieldSchema.type == Schema.Type.RECORD) {
            validateRequiredFieldsInConfig(fieldSchema, false, fieldName)
        }
    }

    private fun throwNonProvidedField(fieldName: String) {
        require(!requiredFieldMissing(fieldName))
        { "Field named $fieldName is a required field in output schema but is not provided in config"}
    }

    private fun validateRequiredFieldsInConfig(outputSchema: Schema, firstCall: Boolean, previousFieldName: String) {
        outputSchema.fields
                .forEach(Consumer { field: Schema.Field ->
                    var fieldName: String = field.name()
                    fieldName = concatPreviousFieldName(firstCall, fieldName, previousFieldName)
                    when (field.primitive()) {
                        true -> saveFieldIfRequired(field, fieldName)
                        false -> validateNonPrimitive(field, fieldName)
                    }
                })
    }

    private fun saveFieldIfRequired(field: Schema.Field, fieldName: String) {
        if (field.required()) {
            throwNonProvidedField(fieldName)
            requiredFieldsOnSchema.add(
                    classMappingMetadata.getFieldMappingByDestination(fieldName))
        }
    }

    private fun concatPreviousFieldName(firstCall: Boolean, fieldName: String, previousFieldName: String): String {
        return if (!firstCall) "$previousFieldName.$fieldName"
        else fieldName
    }

    fun validateRequiredFieldsInConfig(outputSchema: Schema) {
        validateRequiredFieldsInConfig(outputSchema, true, "")
        val sourceToDestination = requiredFieldsOnSchema.map { t ->
            "${t.sourceName} -> ${t.destinationName}"
        }

        logger.info {
            "Required fields on ${outputSchema.name} {source schema} -> {dest schema} are:\n $sourceToDestination"
        }
    }

    private fun Any.getMembers(): Collection<KProperty1<Any, *>> {
        return this.javaClass.kotlin.memberProperties
    }

    private fun reflectiveGetAndValidateMember(obj: Any, memberName: String): Any {
        val member = obj.getMembers().find { kProperty1 -> kProperty1.name == memberName }
        requireNotNull(member, { "Could not find field named <$memberName> in object <$obj>" })

        val memberValue = member.get(obj)
        requireNotNull(memberValue, { "Could not find a value for a required field named: <$memberName> in object: <$obj>" })
        return memberValue
    }

    fun validateRequiredFieldsInRecord(record: SpecificRecord) {
        requiredFieldsOnSchema.forEach {
            var field: Any = record
            val fullFieldName = it.sourceName.split(".").toMutableList()
            fullFieldName.forEach { currentFieldName: String ->
                field = reflectiveGetAndValidateMember(field, currentFieldName)
            }
        }
    }
}