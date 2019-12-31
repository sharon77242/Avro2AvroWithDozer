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

            requiredFieldsValidator = RequiredFieldsValidator(getClassMappingMetadata())
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

    private fun getClassMappingMetadata(): ClassMappingMetadata {
        val classMappingMetadata =
                mapper.mappingMetadata.classMappings.firstOrNull {
                    it.destinationClassName == lastDestinationClassName &&
                            it.sourceClassName == lastSourceClassName &&
                            it.mapId == lastMapId
                }

        requireNotNull(classMappingMetadata, {
            "Could not find proper mapping from files: ${mapper.mapperModelContext.mappingFiles} " +
                    "with mapId: $lastMapId, \nDestinationClass: $lastDestinationClassName " +
                    "and SourceClass: $lastSourceClassName"
        })

        return classMappingMetadata
    }

    private fun newMapping(outputRecord: SpecificRecord, inputRecord: SpecificRecord, mapId: String) =
            lastDestinationClassName != outputRecord.schema.fullName ||
                    lastSourceClassName != inputRecord.schema.fullName ||
                    lastMapId != mapId

    fun <T : SpecificRecord> mapToANewRecord(inputRecord: SpecificRecord, outputSchema: Schema, mapId: String): T? {
        return try {
            val mappedRecord = outputSchema.createNewInstance()
            baseMapper(inputRecord, mappedRecord, mapId, false)
        } catch (e: Exception) {
            logger.error {
                "Could not convert record: ${inputRecord.schema.fullName}:$inputRecord" +
                        " to schema: ${outputSchema.fullName} with mapId: $mapId \n" +
                        "Got Exception $e"
            }
            null
        }
    }

    fun <T : SpecificRecord> mapToAExistingRecord(inputRecord: SpecificRecord, outputRecord: SpecificRecord, mapId: String): T? {
        return try {
            logger.info { "Existing old output record is: $outputRecord" }
            baseMapper(inputRecord, outputRecord, mapId, true)
        } catch (e: Exception) {
            logger.error {
                "Could not convert record: ${inputRecord.schema.fullName}:$inputRecord" +
                        " to schema: ${outputRecord.schema.fullName} with mapId: $mapId \n" +
                        "Got Exception $e"
            }
            null
        }
    }
}