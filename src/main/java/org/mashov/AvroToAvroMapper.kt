package org.mashov

import com.github.dozermapper.core.DozerBeanMapperBuilder
import com.github.dozermapper.core.Mapper
import com.github.dozermapper.core.metadata.ClassMappingMetadata
import mu.KotlinLogging
import org.apache.avro.Schema
import org.apache.avro.specific.SpecificData
import org.apache.avro.specific.SpecificRecord
import org.mashov.AvroUtils.createAndValidateRecord
import org.mashov.AvroUtils.createNewInstance

private val logger = KotlinLogging.logger {}
class AvroToAvroMapper(mapperConfig: String) {
    private val mapper: Mapper = DozerBeanMapperBuilder.create()
            .withMappingFiles(mapperConfig)
            .build()
    private val classMappingMetadata: ClassMappingMetadata = mapper.mappingMetadata.classMappings.first()
    private var requiredFieldsValidator: RequiredFieldsValidator = RequiredFieldsValidator(classMappingMetadata)

    private fun baseMapper(inputRecord: SpecificRecord,
                           outputRecord: SpecificRecord,
                           mapId: String,
                           useDeepCopy: Boolean): SpecificRecord {
        logger.info { "Got new record: $inputRecord" }
        requiredFieldsValidator.validateRequiredFieldsInConfig(outputRecord.schema)
        requiredFieldsValidator.validateRequiredFieldsInRecord(inputRecord)


        val mappedRecord: SpecificRecord =
                when (useDeepCopy) {
                    true -> SpecificData.get().deepCopy(outputRecord.schema, outputRecord)
                    false -> outputRecord
                }

        mapper.map(inputRecord, mappedRecord, mapId)
        logger.info { "Mapped output record is: $outputRecord" }

        return mappedRecord.createAndValidateRecord()
    }

    fun mapToANewRecord(inputRecord: SpecificRecord, outputSchema: Schema, mapId: String): SpecificRecord {
        val mappedRecord = outputSchema.createNewInstance()
        return baseMapper(inputRecord, mappedRecord, mapId, false)
    }

    fun mapToAExistingRecord(inputRecord: SpecificRecord, outputRecord: SpecificRecord, mapId: String): SpecificRecord {
        logger.info { "Existing old output record is: $outputRecord" }
        return baseMapper(inputRecord, outputRecord, mapId, true)
    }
}