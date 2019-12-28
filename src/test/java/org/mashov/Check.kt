package org.mashov

import com.github.dozermapper.core.DozerBeanMapperBuilder
import org.apache.avro.specific.SpecificData
import org.apache.avro.specific.SpecificRecord
import org.junit.jupiter.api.Test
import java.util.*
import java.util.logging.Logger

class Check {
    private val mapper = DozerBeanMapperBuilder.create()
            .withMappingFiles(DEFAULT_MAPPER)
            .build()

    private fun generateInputRecordExample(): BdPerson {
        val child1 = Child.newBuilder().setName("noob").build()
        val child2 = Child.newBuilder().setName("noob2").build()
        val children: MutableList<Child> = ArrayList()
        children.add(child1)
        children.add(child2)
        val additional: MutableMap<String, String> = HashMap()
        additional["shit1"] = "shit2"
        additional["shit3"] = "shit4"
        return BdPerson.newBuilder()
                .setIdentification(
                        Identification.newBuilder()
                              //  .setId(2)
                                .setUsername("sharone")
                                .build())
                .setUsername("mrscarter")
                .setFirstName("Beyonce")
                .setLastName("Knowles-Carter")
                .setBirthdate("1981-09-04")
                .setPhoneNumber("555555555")
                .setMiddleName("kaka")
                .setSex("Man")
                .setCards(Cards.CLUBS)
                .setChildren(children)
                .setAdditional(additional)
                .build()
    }

    private fun generateOutputRecordExample(): BdPersonOut {
        return BdPersonOut.newBuilder()
                .setIdentificationout(
                        IdentificationOut.newBuilder()
                                .setIdout(3)
                                .setUsernameout("sharone1")
                                .build())
                .setHeight(1.84)
                .setCardsout(CardsOut.DIAMONDS)
                .setChildrenout(ArrayList())
                .setAdditionalout(HashMap())
                .build()
    }

    private fun validateRecord(record: SpecificRecord) {
        val newBuilder = record.javaClass.getMethod("newBuilder", record.javaClass)
        val recordBuilder = newBuilder.invoke(record, record)
        recordBuilder.javaClass.getMethod("build").invoke(recordBuilder)
    }

    @Test
    fun mapToANewRecord() {
        val classMappingMetadata = mapper.mappingMetadata.classMappings.first()
        val requiredFieldsChecker = RequiredFieldsValidator(classMappingMetadata)
        requiredFieldsChecker.validateRequiredFieldsInConfig(BdPersonOut.`SCHEMA$`)
        val p = generateInputRecordExample()

        requiredFieldsChecker.validateRequiredFieldsInRecord(p)
        val p1 = mapper.map(p, BdPersonOut::class.java, "firstMapping")

        validateRecord(p1)

        LOGGER.info("p1 is: $p1")
    }

    @Test
    fun mapToAExistingRecord() {
        val classMappingMetadata = mapper.mappingMetadata.classMappings.first()
        val requiredFieldsChecker = RequiredFieldsValidator(classMappingMetadata)
        requiredFieldsChecker.validateRequiredFieldsInConfig(BdPersonOut.`SCHEMA$`)

        val p = generateInputRecordExample()
        LOGGER.info("p is: $p")
        requiredFieldsChecker.validateRequiredFieldsInRecord(p)

        val p1 = generateOutputRecordExample()
        val p1Copy = SpecificData.get().deepCopy(BdPersonOut.`SCHEMA$`, p1)
        LOGGER.info("p1 before conversion is: $p1Copy")

        mapper.map(p, p1Copy, "firstMapping")
        validateRecord(p1Copy)
        LOGGER.info("p1 after builder is: $p1Copy")
    }

    companion object {
        private val LOGGER = Logger.getLogger(Check::javaClass.name)
        private const val DEFAULT_MAPPER = "avroMapper.xml"
    }
}