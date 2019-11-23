package example;

import example.generated.*;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecordBase;

import java.util.*;
import java.util.logging.Logger;

public class Main {
    private final static Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static SpecificRecordBase generateInputRecordExample() {
        Child child1 = Child.newBuilder().setName("noob").build();
        Child child2 = Child.newBuilder().setName("noob2").build();

        List<Child> children = new ArrayList<>();
        children.add(child1);
        children.add(child2);

        Map<String, String> additional = new HashMap<>();
        additional.put("shit1", "shit2");
        additional.put("shit3", "shit4");

        return BdPerson.newBuilder()
                .setIdentification(
                        Identification.newBuilder()
                                //.setId(2)
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
                .build();
    }

    private static SpecificRecordBase generateOutputRecordExample() {
        return BdPersonOut.newBuilder()
                .setIdentificationout(
                        IdentificationOut.newBuilder()
                                //  .setUsernameout("sharone1")
                                .build())
                .setHeight(1.84)
                .setCardsout(CardsOut.DIAMONDS)
                .setChildrenout(new ArrayList<>())
                .setAdditionalout(new HashMap<>())
                .build();
    }

    // TODO: this configuration should read from configfile or json
    private static List<FieldConfiguration> generateExampleConfig() {
        List<FieldConfiguration> fieldConfigurations = new ArrayList<>();
        Queue<String> inputPath;
        Queue<String> outputPath;

        // Try Copy a initialized field on input record
//        inputPath = new LinkedList<>();
//        inputPath.add("identification");
//        inputPath.add("id");
//
//        outputPath = new LinkedList<>();
//        outputPath.add("identificationout");
//        outputPath.add("idout");
//
//        fieldConfigurations.add(new FieldConfiguration(
//                "idout",
//                inputPath,
//                outputPath));

        // Try Copy a non initialized field on input record
        inputPath = new LinkedList<>();
        inputPath.add("identification");
        inputPath.add("username");

        outputPath = new LinkedList<>();
        outputPath.add("identificationout");
        outputPath.add("usernameout");

        fieldConfigurations.add(new FieldConfiguration(
                "usernameout",
                inputPath,
                outputPath));

        inputPath = new LinkedList<>();
        inputPath.add("cards");

        outputPath = new LinkedList<>();
        outputPath.add("cardsout");


        fieldConfigurations.add(new FieldConfiguration(
                "cardsout",
                inputPath,
                outputPath));


        // Try Copy a non initialized field on input record
        inputPath = new LinkedList<>();
        inputPath.add("children");

        outputPath = new LinkedList<>();
        outputPath.add("childrenout");

        fieldConfigurations.add(new FieldConfiguration(
                "childrenout",
                inputPath,
                outputPath));

        inputPath = new LinkedList<>();
        inputPath.add("additional");

        outputPath = new LinkedList<>();
        outputPath.add("additionalout");

        fieldConfigurations.add(new FieldConfiguration(
                "additionalout",
                inputPath,
                outputPath));

        return fieldConfigurations;
    }

    private static void exampleConvertNewRecord() {
        List<FieldConfiguration> fieldConfigurations = generateExampleConfig();
        SpecificRecordBase inputRecord = generateInputRecordExample();
        Schema outSchemaExample = BdPersonOut.SCHEMA$;

        Optional<SpecificRecordBase> outRecord = AvroToAvroConverter.convertToNewRecord(fieldConfigurations, inputRecord, outSchemaExample);
        if (outRecord.isPresent()) {
            LOGGER.info("After converting out new record is: " + outRecord.get());
            return;
        }

        LOGGER.info("converting failed");
    }

    private static void exampleConvertExistingRecord() {
        List<FieldConfiguration> fieldConfigurations = generateExampleConfig();
        SpecificRecordBase inputRecord = generateInputRecordExample();
        SpecificRecordBase outRecordExample = generateOutputRecordExample();

        Optional<SpecificRecordBase> outRecord = AvroToAvroConverter.convertToExistingRecord(fieldConfigurations, inputRecord, outRecordExample);
        if (outRecord.isPresent()) {
            LOGGER.info("After converting out existing record is: " + outRecord.get());
            return;
        }

        LOGGER.info("converting failed");
    }

    public static void main(String[] args) {
        exampleConvertNewRecord();

        exampleConvertExistingRecord();
    }
}
