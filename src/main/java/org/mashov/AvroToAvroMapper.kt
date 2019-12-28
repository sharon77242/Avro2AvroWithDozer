package org.mashov

import com.github.dozermapper.core.DozerBeanMapperBuilder
import com.github.dozermapper.core.Mapper
import mu.KotlinLogging
import org.apache.avro.Schema
import org.apache.avro.specific.SpecificData
import org.apache.avro.specific.SpecificRecord
import org.mashov.AvroUtils.getUnionSchema

private val logger = KotlinLogging.logger {}
class AvroToAvroMapper(mapperConfig: String) {
    private val mapper: Mapper = DozerBeanMapperBuilder.create()
            .withMappingFiles(mapperConfig)
            .build()
    private val classMappingMetadata = mapper.mappingMetadata.classMappings.first()
    private val requiredFieldsValidator = RequiredFieldsValidator(classMappingMetadata)

    private fun validateDestinationRecord(record: SpecificRecord): SpecificRecord {
        val newBuilder = record.javaClass.getMethod("newBuilder", record.javaClass)
        val recordBuilder = newBuilder.invoke(record, record)
        return recordBuilder.javaClass.getMethod("build").invoke(recordBuilder) as SpecificRecord
    }

    private fun createNewInstance(schema: Schema): Any? {
        var schemaCopy: Schema = schema
        if (schema.isUnion) {
            schemaCopy = getUnionSchema(schema)
        }
        return SpecificData.newInstance(Class.forName(schemaCopy.fullName), schema)
    }

    private fun baseMapper(inputRecord: SpecificRecord,
                           outputRecord: SpecificRecord,
                           mapId: String,
                           useDeepCopy: Boolean): SpecificRecord {
        logger.info { "Got new record: $inputRecord" }

        requiredFieldsValidator.validateRequiredFieldsInConfig(outputRecord.schema)
        requiredFieldsValidator.validateRequiredFieldsInRecord(inputRecord)

        val mappedRecord: SpecificRecord =
                if (useDeepCopy) {
                    SpecificData.get().deepCopy(outputRecord.schema, outputRecord)
                } else {
                    outputRecord
                }

        mapper.map(inputRecord, mappedRecord, mapId)

        logger.info { "Mapped output record is: $outputRecord" }

        return validateDestinationRecord(mappedRecord)
    }

    fun mapToANewRecord(inputRecord: SpecificRecord, outputSchema: Schema, mapId: String): SpecificRecord {
        val mappedRecord = createNewInstance(outputSchema) as SpecificRecord

        return baseMapper(inputRecord, mappedRecord, mapId, false)
    }

    fun mapToAExistingRecord(inputRecord: SpecificRecord, outputRecord: SpecificRecord, mapId: String): SpecificRecord {
        logger.info { "Existing old output record is: $outputRecord" }

        return baseMapper(inputRecord, outputRecord, mapId, true)
    }
}