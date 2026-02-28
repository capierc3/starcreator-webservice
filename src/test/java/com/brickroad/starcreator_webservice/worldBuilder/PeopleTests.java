package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.creator.PersonCreator;
import com.brickroad.starcreator_webservice.entity.ud.Person;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
public class PeopleTests extends AbstractCreatorTest {

    @Autowired
    PersonCreator personCreator;

    private static final int PERSON_COUNT = 100;

    private static int isMale = 0;
    private static final Map<String, Integer> origins = new java.util.HashMap<>();

    @Test
    void buildISSWayfarersRest() throws JsonProcessingException {
        System.out.println("---------------------------");
        System.out.println("Building ISS Wayfarers Rest");
        System.out.println("---------------------------");
        System.out.println( "Creating Suites");
        int firstClassSuitesAmt = 60;
        Map<String, Object> firstClassList = Map.of("First Class Suites", createFirstClass(firstClassSuitesAmt));
        saveShipSectionJson(listToJsonString(firstClassList),"ISS_Wayfarers_Rest_Suites");
        System.out.println( "Creating Cabins");
        int businessClassCabinsAmt = 300;
        Map<String, Object> businessClassList = Map.of("Business Class Cabins", createBusinessClass(businessClassCabinsAmt));
        saveShipSectionJson(listToJsonString(businessClassList),"ISS_Wayfarers_Rest_Cabins");
        System.out.println( "Creating Bunks");
        int economyDecks = 3;
        int economyClassBunksPerDeck = 240;
        int sleepRotations = 3;
        Map<String, Object> economyClassList = Map.of("Economy Class Bunks", createEconomyClass(economyDecks,economyClassBunksPerDeck,sleepRotations));
        saveShipSectionJson(listToJsonString(economyClassList),"ISS_Wayfarers_Rest_Bunks");

        int totalPeople = 0;

        System.out.println("---------------------------");;
        System.out.println("    ISS Wayfarer's Rest    ");

        System.out.println("---------------------------");
        System.out.println("----First Class Suites-----");
        System.out.println("---------------------------");
        System.out.println("Number of Suites: " + firstClassSuitesAmt);
        Map<String,List<Person>> firstClassSuites = (Map<String, List<Person>>) firstClassList.get("First Class Suites");
        totalPeople += printSectionStats(firstClassSuites, "First");

        System.out.println("---------------------------");
        System.out.println("---Business Class Cabins---");
        System.out.println("---------------------------");
        System.out.println("Number of Cabins: " + businessClassCabinsAmt);
        Map<String,List<Person>> businessClassCabins = (Map<String, List<Person>>) businessClassList.get("Business Class Cabins");
        totalPeople += printSectionStats(businessClassCabins, "Business");

        System.out.println("---------------------------");
        System.out.println("----Economy Class Bunks----");
        System.out.println("---------------------------");
        System.out.println("Number of Decks: " + economyDecks);
        System.out.println("Number of Bunks per Deck: " + economyClassBunksPerDeck);
        System.out.println("Number of Sleep Rotations: " + sleepRotations);
        Map<String,List<Person>> economyClassBunks = (Map<String, List<Person>>) economyClassList.get("Economy Class Bunks");
        totalPeople += printEconomyClassStats(economyClassBunks);

        System.out.println("---------------------------");
        System.out.println("Total People: " + totalPeople);
        System.out.println("---------------------------");
    }

    @Test
    void testCreatePerson() throws JsonProcessingException {
        Person person = personCreator.createPerson("Male", "English", "adult");
        Map<String, Object> testResults = Map.of("person", person);
        saveJson(listToJsonString(testResults), person.getFullName());
    }

    @Test
    void testCreatePeople() throws JsonProcessingException {
        List<Person> people = new ArrayList<>();
        for (int i = 0; i < PERSON_COUNT; i++) {
            people.add(personCreator.createPerson());
        }
        Map<String, Object> testResults = Map.of("people", people);
        saveJson(listToJsonString(testResults), "people");
    }

    @Test
    void testPeopleProbability() throws JsonProcessingException {
        int peopleAmount = 100_000;
        PerformanceTimer timer = new PerformanceTimer();
        timer.start();
        for (int i = 0; i < peopleAmount; i++) {
            if (i % 10000 == 0 && i != 0) {
                printETA(timer.averageLap(), peopleAmount,i);
            }
            Person person = personCreator.createPerson();
            if (person.getGender().equalsIgnoreCase("MALE")) isMale++;
            origins.put(person.getCulturalOrigin(), origins.getOrDefault(person.getCulturalOrigin(), 0) + 1);
            timer.lap();
        }
        timer.stop();
        System.out.println("------------------------------------------------------------");
        System.out.println("Created " + peopleAmount + " people in " + timer.getFinalTime());
        System.out.println("Probability of male: " + (isMale * 100.0) / peopleAmount + "%");
        System.out.println("------------------------------------------------------------");
        for (String origin : origins.keySet()) {
            System.out.println(origin + ": " + origins.get(origin) + " (" + (origins.get(origin) * 100.0) / peopleAmount + "%)");
        }
        System.out.println("--------------------------------------------------------------");
        System.out.println();

    }

    private Map<String,List<Person>> createFirstClass(int firstClassSuitesAmt) {
        Map<String,List<Person>> firstClassSuites = new HashMap<>();
        for (int i = 0; i < firstClassSuitesAmt; i++) {
            firstClassSuites.put("Suite " + i, new ArrayList<>());
            int roll = RandomUtils.rollD100();
            if (roll >= 90) {
                // 10% chance single person suite
                Person person = personCreator.createPersonInClassRange(PersonCreator.SocialClass.UPPER_CLASS, PersonCreator.SocialClass.NOBILITY);
                firstClassSuites.get("Suite " + i).add(person);
            } else if (roll >= 50) {
                // 40% chance of couple
                Person person1 = personCreator.createPersonInClassRange(PersonCreator.SocialClass.UPPER_MIDDLE_CLASS, PersonCreator.SocialClass.NOBILITY);
                Person person2 = personCreator.createPersonInClassRange(PersonCreator.SocialClass.UPPER_MIDDLE_CLASS, PersonCreator.SocialClass.NOBILITY);
                if (RandomUtils.rollD100() >= 50) {
                    //50% married
                    person2.setLastName(person1.getLastName());
                    person2.setFullName(person2.getFirstName() + " " + person2.getLastName());
                }
                firstClassSuites.get("Suite " + i).add(person1);
                firstClassSuites.get("Suite " + i).add(person2);

            } else {
                // 50% family
                Person parent1 = personCreator.createPersonInClassRange(PersonCreator.SocialClass.UPPER_MIDDLE_CLASS, PersonCreator.SocialClass.NOBILITY);
                Person parent2 = personCreator.createPersonInClassRange(PersonCreator.SocialClass.UPPER_MIDDLE_CLASS, PersonCreator.SocialClass.NOBILITY);
                parent2.setLastName(parent1.getLastName());
                parent2.setFullName(parent2.getFirstName() + " " + parent2.getLastName());
                firstClassSuites.get("Suite " + i).add(parent1);
                firstClassSuites.get("Suite " + i).add(parent2);
                firstClassSuites.get("Suite " + i).addAll(personCreator.createChildren(parent1,parent2,RandomUtils.rollRange(1,3)));
            }
        }
        return firstClassSuites;
    }

    private Map<String,List<Person>> createBusinessClass(int businessClassCabinsAmt) {
        Map<String,List<Person>> businessClassCabins = new HashMap<>();
        for (int i = 0; i < businessClassCabinsAmt; i++) {
            businessClassCabins.put("Cabin " + i, new ArrayList<>());
            int roll = RandomUtils.rollD100();
            if (roll >= 70) {
                Person person = personCreator.createPersonInClassRange(PersonCreator.SocialClass.MIDDLE_CLASS, PersonCreator.SocialClass.UPPER_CLASS);
                businessClassCabins.get("Cabin " + i).add(person);
            } else if (roll >= 50) {
                // 40% chance of couple
                Person person1 = personCreator.createPersonInClassRange(PersonCreator.SocialClass.WORKING_CLASS, PersonCreator.SocialClass.UPPER_CLASS);
                Person person2 = personCreator.createPersonInClassRange(PersonCreator.SocialClass.WORKING_CLASS, PersonCreator.SocialClass.UPPER_CLASS);
                if (RandomUtils.rollD100() >= 50) {
                    // 50% chance they are married
                    person2.setLastName(person1.getLastName());
                    person2.setFullName(person2.getFirstName() + " " + person2.getLastName());
                }
                businessClassCabins.get("Cabin " + i).add(person1);
                businessClassCabins.get("Cabin " + i).add(person2);
            } else {
                // 50% family
                Person parent1 = personCreator.createPersonInClassRange(PersonCreator.SocialClass.WORKING_CLASS, PersonCreator.SocialClass.UPPER_CLASS);
                Person parent2 = personCreator.createPersonInClassRange(PersonCreator.SocialClass.WORKING_CLASS, PersonCreator.SocialClass.UPPER_CLASS);
                parent2.setLastName(parent1.getLastName());
                parent2.setFullName(parent2.getFirstName() + " " + parent2.getLastName());
                businessClassCabins.get("Cabin " + i).add(parent1);
                businessClassCabins.get("Cabin " + i).add(parent2);
                businessClassCabins.get("Cabin " + i).addAll(personCreator.createChildren(parent1,parent2,RandomUtils.rollRange(1,3)));
            }
        }
        return businessClassCabins;
    }

    private Map<String, List<Person>> createEconomyClass(int economyDecks, int economyClassBunksPerDeck, int sleepRotations) {
        Map<String,List<Person>> economyClassBunks = new HashMap<>();
        List<Person> economyClassBunksList = new ArrayList<>();
        int fullLoad = economyDecks * economyClassBunksPerDeck * sleepRotations;
        int singles = (fullLoad * 60) / 100;
        int couples = (fullLoad * 30) / 100;
        int families = fullLoad - singles - couples;
        for (int i = 0; i < singles; i++) {
            Person person = personCreator.createPersonInClassRange(PersonCreator.SocialClass.LOWER_CLASS, PersonCreator.SocialClass.MIDDLE_CLASS);
            economyClassBunksList.add(person);
        }
        for (int i = 0; i < (couples/2); i++) {
            Person person1 = personCreator.createPersonInClassRange(PersonCreator.SocialClass.LOWER_CLASS, PersonCreator.SocialClass.MIDDLE_CLASS);
            Person person2 = personCreator.createPersonInClassRange(PersonCreator.SocialClass.LOWER_CLASS, PersonCreator.SocialClass.MIDDLE_CLASS);
            if (RandomUtils.rollD100() >= 50) {
                // 50% chance they are married
                person2.setLastName(person1.getLastName());
                person2.setFullName(person2.getFirstName() + " " + person2.getLastName());
            }
            economyClassBunksList.add(person1);
            economyClassBunksList.add(person2);
        }
        while (families > 3) {
            Person parent1 = personCreator.createPersonInClassRange(PersonCreator.SocialClass.LOWER_CLASS, PersonCreator.SocialClass.WORKING_CLASS);
            Person parent2 = personCreator.createPersonInClassRange(PersonCreator.SocialClass.LOWER_CLASS, PersonCreator.SocialClass.WORKING_CLASS);
            parent2.setLastName(parent1.getLastName());
            parent2.setFullName(parent2.getFirstName() + " " + parent2.getLastName());
            economyClassBunksList.add(parent1);
            economyClassBunksList.add(parent2);
            families -= 2;
            int highKidAmt = Math.min(families, 3);
            List<Person> kids = personCreator.createChildren(parent1,parent2,RandomUtils.rollRange(1,highKidAmt));
            economyClassBunksList.addAll(kids);
            families -= kids.size();
        }
        economyClassBunksList.sort(PersonCreator.BY_CLASS_ASCENDING);

        int listPos = 0;
        int deckNum = 4;
        String bunkName = "Bunk " + deckNum;
        for (int i = 0; i < economyDecks; i++) {
            for (int j = 0; j < economyClassBunksPerDeck; j++) {
                if (j < 10) bunkName = "Bunk " + deckNum + "00";
                if (j < 100) bunkName = "Bunk " + deckNum + "0";
                if (j >= 100) bunkName = "Bunk " + deckNum;
                economyClassBunks.put(bunkName + j, new ArrayList<>());
                for (int k = 0; k < sleepRotations; k++) {
                    if (economyClassBunksList.size() == listPos) break;
                    economyClassBunks.get(bunkName + j).add(economyClassBunksList.get(listPos));
                    listPos++;
                }
            }
            deckNum++;
        }
        return economyClassBunks;
    }

    private int printSectionStats(Map<String, List<Person>> section, String sectionName) {
        int sectionTotal = 0;
        int singles = 0;
        int couples = 0;
        int families = 0;
        Map<String, Integer> classSpread = new HashMap<>();
        for (List<Person> people : section.values()) {
            sectionTotal += people.size();
            if (people.size() == 1) singles++;
            else if (people.size() == 2) couples++;
            else families++;
            for (Person person : people) {
                classSpread.put(person.getSocialClass().getDisplayName(), classSpread.getOrDefault(person.getSocialClass().getDisplayName(), 0) + 1);
            }
        }
        System.out.println(sectionName + " Class Travelers: " + sectionTotal);
        System.out.println("\tSingles: " + singles);
        System.out.println("\tCouples: " + couples);
        System.out.println("\tFamilies: " + families);
        System.out.println("Class Spread:");
        for (String socialClass : classSpread.keySet()) {
            System.out.println("\t"+socialClass + ": " + classSpread.get(socialClass));
        }
        return sectionTotal;
    }

    private int printEconomyClassStats(Map<String, List<Person>> economyClassBunks) {
        int economyClassTotal = 0;
        Map<String, Integer> bunk4ClassSpread = new HashMap<>();
        Map<String, Integer> bunk5ClassSpread = new HashMap<>();
        Map<String, Integer> bunk6ClassSpread = new HashMap<>();
        int deck4Total = 0;
        int deck5Total = 0;
        int deck6Total = 0;
        for (String bunk : economyClassBunks.keySet()) {
            if (bunk.contains("Bunk 4")) {
                deck4Total += economyClassBunks.get(bunk).size();
                for (Person person : economyClassBunks.get(bunk)) {
                    bunk4ClassSpread.put(person.getSocialClass().getDisplayName(), bunk4ClassSpread.getOrDefault(person.getSocialClass().getDisplayName(), 0) + 1);
                }
            }
            if (bunk.contains("Bunk 5")) {
                deck5Total += economyClassBunks.get(bunk).size();
                for (Person person : economyClassBunks.get(bunk)) {
                    bunk5ClassSpread.put(person.getSocialClass().getDisplayName(), bunk5ClassSpread.getOrDefault(person.getSocialClass().getDisplayName(), 0) + 1);
                }
            }
            if (bunk.contains("Bunk 6")) {
                deck6Total += economyClassBunks.get(bunk).size();
                for (Person person : economyClassBunks.get(bunk)) {
                    bunk6ClassSpread.put(person.getSocialClass().getDisplayName(), bunk6ClassSpread.getOrDefault(person.getSocialClass().getDisplayName(), 0) + 1);
                }
            }
        }
        economyClassTotal = deck4Total + deck5Total + deck6Total;
        System.out.println("Economy Class Travelers: " + economyClassTotal);
        System.out.println("\tBunk 4 Class Travelers: " + deck4Total);
        System.out.println("\tBunk 5 Class Travelers: " + deck5Total);
        System.out.println("\tBunk 6 Class Travelers: " + deck6Total);
        System.out.println("Bunk 4 Class Spread:");
        for (String socialClass : bunk4ClassSpread.keySet()) {
            System.out.println("\t"+socialClass + ": " + bunk4ClassSpread.get(socialClass));
        }
        System.out.println("Bunk 5 Class Spread:");
        for (String socialClass : bunk5ClassSpread.keySet()) {
            System.out.println("\t"+socialClass + ": " + bunk5ClassSpread.get(socialClass));
        }
        System.out.println("Bunk 6 Class Spread:");
        for (String socialClass : bunk6ClassSpread.keySet()) {
            System.out.println("\t"+socialClass + ": " + bunk6ClassSpread.get(socialClass));
        }
        return economyClassTotal;
    }

    protected void saveShipSectionJson(String jsonString, String fileName) {

        File targetFolder = new File("target/creation-jsons/iss_wayfarers_rest/");

        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }

        File jsonFile = new File(targetFolder, fileName+".json");

        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write(jsonString); // Write the JSON content to the file
            System.out.println("JSON file saved successfully to: " + jsonFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing JSON to file: " + e.getMessage());
        }
    }

}
