package org.mashov

import com.github.dozermapper.core.metadata.ClassMappingMetadata
import com.github.dozermapper.core.metadata.FieldMappingMetadata
import org.apache.avro.Schema
import org.apache.avro.specific.SpecificRecord
import java.util.*
import java.util.function.Consumer
import java.util.logging.Logger
import kotlin.reflect.full.memberProperties

class RequiredFieldsValidator(private val classMappingMetadata: ClassMappingMetadata) {
    private val requiredFieldsOnSchema: MutableList<FieldMappingMetadata> = ArrayList()
    private val LOGGER = Logger.getLogger(RequiredFieldsValidator::javaClass.name)

    private fun primitiveField(field: Schema.Field): Boolean {
        val type = field.schema().type
        return type != Schema.Type.RECORD && type != Schema.Type.UNION
    }

    private fun getUnionSchema(schema: Schema): Schema {
        return schema.types
                .stream()
                .filter { schema1: Schema -> schema1.type != Schema.Type.NULL }
                .reduce { a: Schema, b: Schema -> throw IllegalStateException("Multiple elements: $a, $b") }
                .get()
    }

    private fun requiredFieldMissing(field: String): Boolean {
        return try {
            classMappingMetadata.getFieldMappingByDestination(field)
            false
        } catch (e: Exception) {
            true
        }
    }

    private fun unionHasInnerFieldsOnMapping(fieldName: String): Boolean{
        classMappingMetadata.fieldMappings.forEach {
            fieldMappingMetadata: FieldMappingMetadata? ->
            if (fieldMappingMetadata!!.destinationName.contains(fieldName)){
                return true
            }
        }

        return false
    }

    private fun validateNonPrimitive(field: Schema.Field, fieldName: String) {
        var fieldSchema = field.schema()
        if (fieldSchema.isUnion) {
            if (requiredFieldMissing(fieldName) && !unionHasInnerFieldsOnMapping(fieldName)) {
                return
            }

            fieldSchema = getUnionSchema(fieldSchema)
        }
        if (fieldSchema.type == Schema.Type.RECORD) {
            validateRequiredFieldsInConfig(fieldSchema, false, fieldName)
        }
    }

    private fun throwNonProvidedField(fieldName: String) {
        if (requiredFieldMissing(fieldName)) {
            throw RuntimeException(
                    "Field named $fieldName is a required field in output schema but is not provided in config")
        }
    }

    private fun validateRequiredFieldsInConfig(outputSchema: Schema, firstCall: Boolean, currentFieldName: String) {
        outputSchema.fields
                .forEach(Consumer { field: Schema.Field ->
                    var fieldName = field.name()
                    if (!firstCall) {
                        fieldName = "$currentFieldName.$fieldName"
                    }

                    if (primitiveField(field)) {
                        val requiredField = !field.hasDefaultValue()
                        if (requiredField) {
                            throwNonProvidedField(fieldName)
                            requiredFieldsOnSchema.add(
                                    classMappingMetadata.getFieldMappingByDestination(fieldName))
                        }
                    } else {
                        validateNonPrimitive(field, fieldName)
                    }
                })
    }

    fun validateRequiredFieldsInConfig(outputSchema: Schema) {
        validateRequiredFieldsInConfig(outputSchema, true, "")

        LOGGER.info("Required fields on ${outputSchema.name} {source schema} -> {dest schema}")

        requiredFieldsOnSchema.forEach { t ->
            LOGGER.info("${t.sourceName} -> ${t.destinationName}")
        }
    }

    private fun reflectiveGetAndValidateMember(obj: Any, memberName: String): Any {
        val member =  obj.javaClass.kotlin.memberProperties.find { kProperty1 -> kProperty1.name == memberName }
        requireNotNull(member, {"Could not find field named <$memberName> in object <$obj>"})

        val memberValue = member.get(obj)
        requireNotNull(memberValue, {"Could not find a value for a required field named: <$memberName> in object: <$obj>"})
        return memberValue
    }

    fun validateRequiredFieldsInRecord(record: SpecificRecord) {
        requiredFieldsOnSchema.forEach {fieldMappingMetadata: FieldMappingMetadata ->
            var field: Any = record
            val fullFieldName = fieldMappingMetadata.sourceName.split(".").toMutableList()
            fullFieldName.forEach { currentFieldName: String ->
                field = reflectiveGetAndValidateMember(field, currentFieldName)
            }
        }
    }
}