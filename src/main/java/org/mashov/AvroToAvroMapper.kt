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
    private var lastSourceClassName: String = ""
    private var lastDestinationClassName: String = ""
    private var lastMapId: String = ""
    private lateinit var requiredFieldsValidator: RequiredFieldsValidator

    private fun <T: Any> baseMapper(inputRecord: SpecificRecord,
                                              outputRecord: SpecificRecord,
                                              mapId: String,
                                              useDeepCopy: Boolean):  T{
        logger.info { "Got new record: $inputRecord" }

        if (newMapping(outputRecord, inputRecord, mapId)) {
            lastDestinationClassName = outputRecord.schema.fullName
            lastSourceClassName = inputRecord.schema.fullName
            lastMapId = mapId

            requiredFieldsValidator = RequiredFieldsValidator(getClassMappingMetadata(outputRecord, inputRecord))
            requiredFieldsValidator.validateRequiredFieldsInConfig(outputRecord.schema)
        }

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

    private fun getClassMappingMetadata(outputRecord: SpecificRecord, inputRecord: SpecificRecord): ClassMappingMetadata {
        return mapper.mappingMetadata.classMappings.first {
            it.destinationClassName == outputRecord.schema.fullName && it.sourceClassName == inputRecord.schema.fullName
        }
    }

    private fun newMapping(outputRecord: SpecificRecord, inputRecord: SpecificRecord, mapId: String) =
            lastDestinationClassName != outputRecord.schema.fullName ||
                    lastSourceClassName != inputRecord.schema.fullName ||
                    lastMapId != mapId

    fun <T: Any> mapToANewRecord(inputRecord: SpecificRecord, outputSchema: Schema, mapId: String): T {
        val mappedRecord = outputSchema.createNewInstance()
        return baseMapper(inputRecord, mappedRecord, mapId, false) as T
    }

    fun mapToAExistingRecord(inputRecord: SpecificRecord, outputRecord: SpecificRecord, mapId: String): SpecificRecord {
        logger.info { "Existing old output record is: $outputRecord" }
        return baseMapper(inputRecord, outputRecord, mapId, true)
    }
}