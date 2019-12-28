package org.mashov

import org.junit.jupiter.api.Test
import org.mashov.generated.*
import java.util.*

private const val DEFAULT_MAPPER = "avroMapper.xml"
class Check {
    private val avroToAvroMapper = AvroToAvroMapper(DEFAULT_MAPPER)

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
                                .setId(2)
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

    @Test
    fun mapToANewRecord() {
        val p = generateInputRecordExample()
        avroToAvroMapper.mapToANewRecord(p, BdPersonOut.`SCHEMA$`, "firstMapping")
    }

    @Test
    fun mapToAExistingRecord() {
        val p = generateInputRecordExample()
        val p1 = generateOutputRecordExample()
        avroToAvroMapper.mapToAExistingRecord(p, p1, "firstMapping")
    }

}