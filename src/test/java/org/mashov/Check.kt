package org.mashov

import org.junit.jupiter.api.Test
import org.mashov.generated.*
import java.util.*
import kotlin.test.assertEquals

private const val DEFAULT_MAPPER = "avroTypesMapper.xml"
class Check {
    private var avroToAvroMapper = AvroToAvroMapper(DEFAULT_MAPPER)

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
    fun mapRecord() {
    }

    @Test
    fun mapEnum() {
        val inputRecord: EnumIn = EnumIn.newBuilder().setCards(Cards.DIAMONDS).build()
        val expectedOutputRecord: EnumOut = EnumOut.newBuilder().setCardsout(Cards.DIAMONDS).build()
        val actualOutputRecord: EnumOut =
                avroToAvroMapper.mapToANewRecord(inputRecord, EnumOut.`SCHEMA$`, "enumMapping")
        assertEquals(expectedOutputRecord, actualOutputRecord, "Enum mapping failed")
    }

    @Test
    fun mapArray() {
        val child1 = Child.newBuilder().setName("noob").build()
        val child2 = Child.newBuilder().setName("noob2").build()
        val children: MutableList<Child> = ArrayList()
        children.add(child1)
        children.add(child2)

        val inputRecord: ArrayIn = ArrayIn.newBuilder().setChildrens(children).build()
        val expectedOutputRecord: ArrayOut = ArrayOut.newBuilder().setChildrensout(children).build()
        val actualOutputRecord: ArrayOut =
                avroToAvroMapper.mapToANewRecord(inputRecord, ArrayOut.`SCHEMA$`, "arrayMapping")
        assertEquals(expectedOutputRecord, actualOutputRecord, "array mapping failed")
    }

    @Test
    fun mapAMap() {
        val additional: MutableMap<String, String> = HashMap()
        additional["shit1"] = "shit2"
        additional["shit3"] = "shit4"

        val inputRecord: MapIn = MapIn.newBuilder().setAdditional(additional).build()
        val expectedOutputRecord: MapOut = MapOut.newBuilder().setAdditionalout(additional).build()
        val actualOutputRecord: MapOut =
                avroToAvroMapper.mapToANewRecord(inputRecord, MapOut.`SCHEMA$`, "mapMapping")
        assertEquals(expectedOutputRecord, actualOutputRecord, "array mapping failed")
    }

    @Test
    fun mapUnion() {
    }

    @Test
    fun mapFixed() {
    }

    @Test
    fun mapString() {
    }

    @Test
    fun mapBytes() {
    }

    @Test
    fun mapInt() {
    }

    @Test
    fun mapLong() {
    }

    @Test
    fun mapFloat() {
    }


    @Test
    fun mapDouble() {
    }

    @Test
    fun mapBoolean() {
    }

    @Test
    fun mapNull() {
    }

    @Test
    fun mapDate() {
    }

//    @Test
//    fun mapArray() {
//        val inputRecord: ArrayIn = ArrayIn.newBuilder().setCards(Cards.DIAMONDS).build()
//        val expectedOutputRecord:EnumOut = EnumOut.newBuilder().setCardsout(Cards.DIAMONDS).build()
//        val actualOutputRecord =
//                avroToAvroMapper.mapToANewRecord(inputRecord, EnumOut.`SCHEMA$`, "enumMapping")
//        assertEquals(expectedOutputRecord, actualOutputRecord, "Enum mapping failed")
//    }


//    @Test
//    fun mapToANewRecord() {
//        val p = generateInputRecordExample()
//        avroToAvroMapper.mapToANewRecord(p, BdPersonOut.`SCHEMA$`, "firstMapping")
//    }
//
//    @Test
//    fun mapToAExistingRecord() {
//        val p = generateInputRecordExample()
//        val p1 = generateOutputRecordExample()
//        avroToAvroMapper.mapToAExistingRecord(p, p1, "firstMapping")
//    }

}