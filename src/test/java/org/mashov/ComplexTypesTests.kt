package org.mashov

import org.junit.jupiter.api.Test
import org.mashov.generated.*
import kotlin.test.assertEquals

private const val DEFAULT_MAPPER = "avroComplexTypesMapper.xml"

class ComplexTypesTests {
    private var avroToAvroMapper = AvroToAvroMapper(DEFAULT_MAPPER)

//    private fun generateInputRecordExample(): BdPerson {
//        val child1 = Child.newBuilder().setName("noob").build()
//        val child2 = Child.newBuilder().setName("noob2").build()
//        return BdPerson.newBuilder()
//                .setIdentification(
//                        Identification.newBuilder()
//                                .setId(2)
//                                .setUsername("sharone")
//                                .build())
//                .setUsername("mrscarter")
//                .setFirstName("Beyonce")
//                .setLastName("Knowles-Carter")
//                .setBirthdate("1981-09-04")
//                .setPhoneNumber("555555555")
//                .setMiddleName("kaka")
//                .setSex("Man")
//                .setCards(Cards.CLUBS)
//                .setChildren(arrayListOf(child1, child2))
//                .setAdditional(mapOf("shit1" to "shit2", "shit3" to "shit4"))
//                .build()
//    }
//
//    private fun generateOutputRecordExample(): BdPersonOut {
//        return BdPersonOut.newBuilder()
//                .setIdentificationout(
//                        IdentificationOut.newBuilder()
//                                .setIdout(3)
//                                .setUsernameout("sharone1")
//                                .build())
//                .setHeight(1.84)
//                .setCardsout(CardsOut.DIAMONDS)
//                .setChildrenout(ArrayList())
//                .setAdditionalout(HashMap())
//                .build()
//    }

    @Test
    fun mapRecord() {
    }

    @Test
    fun mapArray() {
        val child1 = Child.newBuilder().setName("noob").build()
        val child2 = Child.newBuilder().setName("noob2").build()
        val children = arrayListOf(child1, child2)
        val inputRecord: ArrayIn = ArrayIn.newBuilder().setChildrens(children).build()
        val expectedOutputRecord: ArrayOut = ArrayOut.newBuilder().setChildrensout(children).build()

        val actualOutputRecord =
                avroToAvroMapper.mapToANewRecord<ArrayOut>(
                        inputRecord, ArrayOut.`SCHEMA$`, "arrayMapping")
        assertEquals(expectedOutputRecord, actualOutputRecord, "array mapping failed")
    }

    @Test
    fun mapAMap() {
        val additional = mapOf("shit1" to "shit2", "shit3" to "shit4")

        val inputRecord: MapIn = MapIn.newBuilder().setAdditional(additional).build()
        val expectedOutputRecord: MapOut = MapOut.newBuilder().setAdditionalout(additional).build()
        val actualOutputRecord =
                avroToAvroMapper.mapToANewRecord<MapOut>(inputRecord, MapOut.`SCHEMA$`, "mapMapping")
        assertEquals(expectedOutputRecord, actualOutputRecord, "array mapping failed")
    }

    @Test
    fun mapUnion() {
    }

    @Test
    fun mapFixed() {
        val inputRecord = FixedIn.newBuilder().setFixes(md5()).build()
        val expectedOutputRecord = FixedOut.newBuilder().setFixesout(md5Out()).build()

        val actualOutputRecord =
                avroToAvroMapper.mapToANewRecord<FixedOut>(inputRecord, FixedOut.`SCHEMA$`, "fixedMapping")
        assertEquals(expectedOutputRecord, actualOutputRecord, "Fixed mapping failed")
    }

    @Test
    fun mapDate() {
        val currentDate = java.util.Calendar.getInstance().timeInMillis
        val inputRecord = DateIn.newBuilder().setDate(currentDate).build()
        val expectedOutputRecord = DateOut.newBuilder().setDateout(currentDate).build()

        val actualOutputRecord =
                avroToAvroMapper.mapToANewRecord<DateOut>(inputRecord, DateOut.`SCHEMA$`, "dateMapping")
        assertEquals(expectedOutputRecord, actualOutputRecord, "Date mapping failed")
    }

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