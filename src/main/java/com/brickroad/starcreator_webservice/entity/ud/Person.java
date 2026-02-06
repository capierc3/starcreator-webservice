package com.brickroad.starcreator_webservice.entity.ud;

import com.brickroad.starcreator_webservice.creator.PersonCreator;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {

    // Identity
    private String firstName;
    private String lastName;
    private String fullName;
    private String nickname;

    // Demographics
    private String gender;
    private int age;
    private String ageCategory;  // child, young adult, adult, middle-aged, elder
    private String culturalOrigin;
    private String maternalOrigin;
    private String paternalOrigin;
    private boolean isMixedHeritage;

    // Physical
    private String bodyType;
    private String hairColor;
    private String eyeColor;
    private String distinguishingFeature;
    private int heightCm;
    private String heightDescription;

    // Personality
    private String primaryTrait;
    private String secondaryTrait;
    private String flaw;
    private String motivation;

    // Background
    private String occupation;
    private PersonCreator.SocialClass socialClass;
    private String education;

    // Misc flavor
    private String quirk;
    private String hobby;
    private String fear;

    public String getBriefDescription() {
        return String.format("%s, a %d-year-old %s %s %s. %s and %s, with a tendency toward %s.",
                fullName, age, ageCategory, socialClass.getDisplayName(), occupation,
                primaryTrait, secondaryTrait, flaw);
    }

    public String getDetailedDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Name: %s", fullName));
        if (nickname != null) sb.append(String.format(" (%s)", nickname));
        sb.append("\n");
        sb.append(String.format("Age: %d (%s)\n", age, ageCategory));
        sb.append(String.format("Gender: %s\n", gender));
        if (isMixedHeritage) {
            sb.append(String.format("Heritage: %s (maternal) / %s (paternal)\n", maternalOrigin, paternalOrigin));
        } else {
            sb.append(String.format("Origin: %s\n", culturalOrigin));
        }
        sb.append(String.format("Occupation: %s (%s)\n", occupation, socialClass.getDisplayName()));
        sb.append(String.format("Build: %s, %s\n", bodyType, heightDescription));
        sb.append(String.format("Appearance: %s hair, %s eyes", hairColor, eyeColor));
        if (distinguishingFeature != null) sb.append(String.format(", %s", distinguishingFeature));
        sb.append("\n");
        sb.append(String.format("Personality: %s, %s\n", primaryTrait, secondaryTrait));
        sb.append(String.format("Flaw: %s\n", flaw));
        sb.append(String.format("Motivation: %s\n", motivation));
        if (quirk != null) sb.append(String.format("Quirk: %s\n", quirk));
        if (hobby != null) sb.append(String.format("Hobby: %s\n", hobby));
        if (fear != null) sb.append(String.format("Fear: %s\n", fear));
        return sb.toString();
    }
}
