package org.mashov

import org.junit.jupiter.api.Test
import org.mashov.generated.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val DEFAULT_MAPPER = "avroTypesMapper.xml"

class Check {
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
    fun mapEnum() {
        val inputRecord = EnumIn.newBuilder().setCard(Card.DIAMONDS).build()
        val expectedOutputRecord = EnumOut.newBuilder().setCardout(Card.DIAMONDS).build()

        val actualOutputRecord =
                avroToAvroMapper.mapToANewRecord<EnumOut>(inputRecord, EnumOut.`SCHEMA$`, "enumMapping")
        assertEquals(expectedOutputRecord, actualOutputRecord, "Enum mapping failed")
    }

    @Test
    fun mapEnumWithDifferentTypesSameValue() {
        val inputRecord: EnumIn = EnumIn.newBuilder().setCard(Card.DIAMONDS).build()
        val expectedOutputRecord =
                EnumOutDifferentType.newBuilder().setCardout(CardOut.DIAMONDS).build()

        val actualOutputRecord =
                avroToAvroMapper.mapToANewRecord<EnumOutDifferentType>(
                        inputRecord, EnumOutDifferentType.`SCHEMA$`, "enumDifferentMapping")
        assertEquals(expectedOutputRecord, actualOutputRecord, "Enum mapping failed")
    }

    @Test
    fun mapEnumWithDifferentTypesAndValue() {
        val inputRecord: EnumIn = EnumIn.newBuilder().setCard(Card.ACE).build()

        val actualOutputRecord =
                avroToAvroMapper.mapToANewRecord<EnumOutDifferentType>(
                        inputRecord, EnumOutDifferentType.`SCHEMA$`, "enumDifferentMapping")
        assertNull(actualOutputRecord, "Enum mapping failed, input record has extra field")
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
    fun mapString() {
        val inputRecord = StringIn.newBuilder().setUsername("sharone").build()
        val expectedOutputRecord = StringOut.newBuilder().setUsernameout("sharone").build()

        val actualOutputRecord =
                avroToAvroMapper.mapToANewRecord<StringOut>(inputRecord, StringOut.`SCHEMA$`, "stringMapping")
        assertEquals(expectedOutputRecord, actualOutputRecord, "String mapping failed")
    }

    @Test
    fun mapBytes() {
//        val username = "sharone"
//        val usernameBytes = ByteBuffer.wrap(username.toByteArray())
//
//        val inputRecord = BytesIn.newBuilder().setUsername(usernameBytes).build()
//        val expectedOutputRecord = BytesOut.newBuilder().setUsernameout(usernameBytes).build()
//
//        val actualOutputRecord =
//                avroToAvroMapper.mapToANewRecord<BytesOut>(inputRecord, BytesOut.`SCHEMA$`, "bytesMapping")
//        assertEquals(expectedOutputRecord, actualOutputRecord, "byte mapping failed")
        //TODO: currently unsupported for support search for "dozer-mapping-of-class-with-no-default-constructor"
    }

    @Test
    fun mapInt() {
        val inputRecord = IntIn.newBuilder().setAge(25).build()
        val expectedOutputRecord = IntOut.newBuilder().setAgeout(25).build()

        val actualOutputRecord =
                avroToAvroMapper.mapToANewRecord<IntOut>(inputRecord, IntOut.`SCHEMA$`, "intMapping")
        assertEquals(expectedOutputRecord, actualOutputRecord, "Int mapping failed")
    }

    @Test
    fun mapLong() {
        val paiNumber = 3.14159265358979323846264338327950288419716939937510.toLong()
        val inputRecord = LongIn.newBuilder()
                .setPainumber(paiNumber).build()
        val expectedOutputRecord = LongOut.newBuilder().setPainumberout(paiNumber).build()

        val actualOutputRecord =
                avroToAvroMapper.mapToANewRecord<LongOut>(inputRecord, LongOut.`SCHEMA$`, "longMapping")
        assertEquals(expectedOutputRecord, actualOutputRecord, "Long mapping failed")
    }

    @Test
    fun mapFloat() {
        val inputRecord = FloatIn.newBuilder().setAngle(220.15F).build()
        val expectedOutputRecord = FloatOut.newBuilder().setAngleout(220.15F).build()

        val actualOutputRecord =
                avroToAvroMapper.mapToANewRecord<FloatOut>(inputRecord, FloatOut.`SCHEMA$`, "floatMapping")
        assertEquals(expectedOutputRecord, actualOutputRecord, "Float mapping failed")
    }


    @Test
    fun mapDouble() {
        val inputRecord = DoubleIn.newBuilder().setAngle(250.1).build()
        val expectedOutputRecord = DoubleOut.newBuilder().setAngleout(250.1).build()

        val actualOutputRecord =
                avroToAvroMapper.mapToANewRecord<DoubleOut>(inputRecord, DoubleOut.`SCHEMA$`, "doubleMapping")
        assertEquals(expectedOutputRecord, actualOutputRecord, "Double mapping failed")
    }

    @Test
    fun mapBoolean() {
        val inputRecord = BooleanIn.newBuilder().setAmibalding(true).build()
        val expectedOutputRecord = BooleanOut.newBuilder().setAmibaldingout(true).build()

        val actualOutputRecord =
                avroToAvroMapper.mapToANewRecord<BooleanOut>(inputRecord, BooleanOut.`SCHEMA$`, "booleanMapping")
        assertEquals(expectedOutputRecord, actualOutputRecord, "Boolean mapping failed")
    }

    @Test
    fun mapNull() {
        val inputRecord = NullIn.newBuilder().setIamnull(null).build()

        val actualOutputRecord =
                avroToAvroMapper.mapToANewRecord<NullOut>(inputRecord, NullOut.`SCHEMA$`, "nullMapping")

        // There's no better way i found for testing null type not being mapped
        assertNull(actualOutputRecord, "Null mapping failed")
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