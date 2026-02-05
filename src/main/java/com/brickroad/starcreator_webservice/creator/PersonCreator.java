package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ref.NameOriginRef;
import com.brickroad.starcreator_webservice.entity.ref.NameRef;
import com.brickroad.starcreator_webservice.entity.ud.Person;
import com.brickroad.starcreator_webservice.repository.NameOriginRefRepository;
import com.brickroad.starcreator_webservice.repository.NameRefRepository;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


@Component
public class PersonCreator {

    private final NameRefRepository nameRefRepository;
    private final NameOriginRefRepository originRefRepository;

    // Cached data
    private List<NameRef> cachedFirstNames;
    private List<NameRef> cachedLastNames;
    private List<NameOriginRef> cachedOrigins;

    private static final int MIXED_HERITAGE_CHANCE = 15; // 15% chance

    // Attribute arrays for random selection
    private static final String[] BODY_TYPES = {
            "Slim", "Athletic", "Average", "Stocky", "Muscular", "Heavyset", "Wiry", "Lanky", "Petite", "Broad-shouldered"
    };

    private static final String[] HAIR_COLORS = {
            "Black", "Dark brown", "Brown", "Light brown", "Auburn", "Red", "Strawberry blonde",
            "Blonde", "Platinum blonde", "Gray", "White", "Salt-and-pepper", "Bald"
    };

    private static final String[] EYE_COLORS = {
            "Brown", "Dark brown", "Hazel", "Green", "Blue", "Gray", "Amber", "Black"
    };

    private static final String[] DISTINGUISHING_FEATURES = {
            "a prominent scar", "numerous freckles", "a birthmark", "a crooked nose",
            "a missing finger", "heterochromia", "dimples", "a beauty mark",
            "weathered skin", "calloused hands", "tattoos", "piercing eyes",
            "a warm smile", "a stern expression", "laugh lines", null, null, null // nulls for "no feature"
    };

    private static final String[] PRIMARY_TRAITS = {
            "Ambitious", "Cautious", "Charismatic", "Compassionate", "Confident", "Creative",
            "Curious", "Determined", "Diplomatic", "Disciplined", "Honest", "Humble",
            "Idealistic", "Independent", "Intelligent", "Intuitive", "Loyal", "Methodical",
            "Optimistic", "Patient", "Perceptive", "Practical", "Resourceful", "Stoic", "Witty"
    };

    private static final String[] SECONDARY_TRAITS = {
            "reserved", "outgoing", "analytical", "emotional", "adventurous", "traditional",
            "pragmatic", "romantic", "skeptical", "trusting", "meticulous", "spontaneous",
            "serious", "playful", "gentle", "fierce", "eloquent", "soft-spoken"
    };

    private static final String[] FLAWS = {
            "arrogance", "stubbornness", "impatience", "jealousy", "greed", "cowardice",
            "recklessness", "cynicism", "naivety", "perfectionism", "vindictiveness",
            "dishonesty", "laziness", "pride", "distrust", "obsessiveness", "short temper",
            "indecisiveness", "selfishness", "pessimism"
    };

    private static final String[] MOTIVATIONS = {
            "Seeking wealth", "Protecting family", "Pursuing knowledge", "Gaining power",
            "Finding love", "Achieving fame", "Seeking revenge", "Escaping the past",
            "Proving themselves", "Serving a cause", "Discovering truth", "Creating legacy",
            "Finding belonging", "Maintaining order", "Seeking freedom", "Redeeming themselves"
    };

    private static final String[] OCCUPATIONS = {
            "Merchant", "Soldier", "Scholar", "Farmer", "Artisan", "Healer", "Priest",
            "Navigator", "Engineer", "Pilot", "Diplomat", "Smuggler", "Bounty hunter",
            "Administrator", "Scientist", "Mechanic", "Cook", "Entertainer", "Guard",
            "Miner", "Doctor", "Teacher", "Trader", "Courier", "Technician"
    };

    private static final String[] SOCIAL_CLASSES = {
            "Lower class", "Working class", "Middle class", "Upper middle class", "Upper class", "Nobility"
    };

    private static final String[] EDUCATION_LEVELS = {
            "Self-taught", "Basic education", "Trade apprenticeship", "Formal schooling",
            "University educated", "Academy trained", "Military training", "Guild certified"
    };

    private static final String[] QUIRKS = {
            "Always hums while working", "Collects unusual objects", "Speaks in proverbs",
            "Constantly fidgets", "Has a signature catchphrase", "Superstitious about certain things",
            "Never sits with back to door", "Obsessed with punctuality", "Terrible with names",
            "Talks to themselves", "Compulsive organizer", "Night owl", "Early riser",
            "Nervous laughter", "Cracks knuckles when thinking"
    };

    private static final String[] HOBBIES = {
            "Reading", "Gambling", "Cooking", "Music", "Stargazing", "Gardening",
            "Woodworking", "Writing", "Chess", "Hunting", "Fishing", "Collecting",
            "Tinkering", "Poetry", "Painting", "Exercise", "Meditation", "Cards"
    };

    private static final String[] FEARS = {
            "Heights", "Enclosed spaces", "Deep water", "The dark", "Being forgotten",
            "Failure", "Abandonment", "Loss of control", "Public speaking", "Intimacy",
            "The unknown", "Insects", "Death", "Crowds", "Being alone"
    };

    private static final String[] NICKNAMES_MALE = {
            "Big", "Red", "Slim", "Doc", "Chief", "Duke", "Ace", "Bear", "Wolf", "Hawk"
    };

    private static final String[] NICKNAMES_FEMALE = {
            "Red", "Slim", "Doc", "Ace", "Raven", "Rose", "Star", "Lucky", "Grace", "Storm"
    };

    private static final String[] LOWER_CLASS_OCCUPATIONS = {
            "Laborer", "Servant", "Street vendor", "Scavenger", "Dockworker",
            "Cleaner", "Farm hand", "Miner", "Factory worker"
    };

    private static final String[] WORKING_CLASS_OCCUPATIONS = {
            "Farmer", "Mechanic", "Cook", "Guard", "Miner", "Courier",
            "Technician", "Driver", "Carpenter", "Welder", "Electrician"
    };

    private static final String[] MIDDLE_CLASS_OCCUPATIONS = {
            "Merchant", "Artisan", "Teacher", "Clerk", "Nurse", "Pilot",
            "Technician", "Trader", "Accountant", "Engineer", "Programmer"
    };

    private static final String[] UPPER_MIDDLE_CLASS_OCCUPATIONS = {
            "Doctor", "Lawyer", "Scientist", "Diplomat", "Scholar", "Architect",
            "Professor", "Administrator", "Manager", "Executive", "Consultant"
    };

    private static final String[] UPPER_CLASS_OCCUPATIONS = {
            "Investor", "Executive", "Diplomat", "Politician", "Industrialist",
            "Ship owner", "Estate owner", "Art collector", "Philanthropist"
    };

    private static final String[] NOBILITY_OCCUPATIONS = {
            "Aristocrat", "Lord", "Lady", "Duke", "Duchess", "Count", "Countess",
            "Governor", "Ambassador", "Chancellor", "Regent", "Heir"
    };

    private static final String[] LOWER_CLASS_EDUCATION = {
            "None", "Self-taught", "Basic education"
    };

    private static final String[] WORKING_CLASS_EDUCATION = {
            "Basic education", "Trade apprenticeship", "Self-taught", "Vocational training"
    };

    private static final String[] MIDDLE_CLASS_EDUCATION = {
            "Formal schooling", "Trade apprenticeship", "Guild certified", "Technical college"
    };

    private static final String[] UPPER_MIDDLE_CLASS_EDUCATION = {
            "University educated", "Academy trained", "Professional certification", "Graduate degree"
    };

    private static final String[] UPPER_CLASS_EDUCATION = {
            "University educated", "Private tutoring", "Elite academy", "Multiple degrees"
    };

    private static final String[] NOBILITY_EDUCATION = {
            "Private tutoring", "Elite academy", "Royal education", "Finishing school"
    };

    @Autowired
    public PersonCreator(NameRefRepository nameRefRepository, NameOriginRefRepository originRefRepository) {
        this.nameRefRepository = nameRefRepository;
        this.originRefRepository = originRefRepository;
    }

    @PostConstruct
    public void init() {
        refreshCache();
    }

    public void refreshCache() {
        cachedFirstNames = nameRefRepository.findAllFirstNames();
        cachedLastNames = nameRefRepository.findAllLastNames();
        cachedOrigins = originRefRepository.findAllOrdered();
    }

    public Person createPerson() {
        return createPerson(null, null, null);
    }

    public Person createPerson(String gender) {
        return createPerson(gender, null, null);
    }

    public Person createPerson(String gender, String origin) {
        return createPerson(gender, origin, null);
    }

    public Person createPerson(String gender, String origin, String ageRange) {
        return createPerson(gender, origin, ageRange, null);
    }

    public Person createPerson(SocialClass socialClass) {
        return createPerson(null, null, null, socialClass);
    }

    public Person createPerson(String gender, SocialClass socialClass) {
        return createPerson(gender, null, null, socialClass);
    }

    public Person createPersonWithOriginAndClass(String origin, SocialClass socialClass) {
        return createPerson(null, origin, null, socialClass);
    }

    public Person createPerson(String gender, String origin, String ageRange, SocialClass socialClass) {
        // Random chance of mixed heritage (only if origin wasn't specifically requested)
        if (origin == null && RandomUtils.rollD100() <= MIXED_HERITAGE_CHANCE) {
            return createRandomMixedHeritagePerson(gender, ageRange, socialClass);
        }

        Person.PersonBuilder builder = Person.builder();

        // Determine gender
        String selectedGender = gender != null ? gender : (RandomUtils.rollD100() <= 50 ? "male" : "female");
        builder.gender(selectedGender);

        // Determine cultural origin
        String selectedOrigin = origin != null ? origin : selectRandomOrigin();
        builder.culturalOrigin(selectedOrigin);
        builder.isMixedHeritage(false);

        // Generate name based on gender and origin
        NameRef firstName = selectFirstName(selectedGender, selectedOrigin);
        NameRef lastName = selectLastName(selectedOrigin);

        builder.firstName(firstName.getName());
        builder.lastName(lastName.getName());
        builder.fullName(firstName.getName() + " " + lastName.getName());

        // Maybe add a nickname (20% chance)
        if (RandomUtils.rollD100() <= 20) {
            String[] nicknames = "male".equals(selectedGender) ? NICKNAMES_MALE : NICKNAMES_FEMALE;
            builder.nickname(nicknames[RandomUtils.rollRange(0, nicknames.length - 1)]);
        }

        // Generate age
        int[] ageData = generateAge(ageRange);
        builder.age(ageData[0]);
        builder.ageCategory(getAgeCategory(ageData[0]));

        // Physical attributes
        builder.bodyType(BODY_TYPES[RandomUtils.rollRange(0, BODY_TYPES.length - 1)]);
        builder.hairColor(selectHairColor(ageData[0]));
        builder.eyeColor(EYE_COLORS[RandomUtils.rollRange(0, EYE_COLORS.length - 1)]);
        builder.distinguishingFeature(DISTINGUISHING_FEATURES[RandomUtils.rollRange(0, DISTINGUISHING_FEATURES.length - 1)]);

        int height = generateHeight(selectedGender);
        builder.heightCm(height);
        builder.heightDescription(describeHeight(height, selectedGender));

        // Personality
        builder.primaryTrait(PRIMARY_TRAITS[RandomUtils.rollRange(0, PRIMARY_TRAITS.length - 1)]);
        builder.secondaryTrait(SECONDARY_TRAITS[RandomUtils.rollRange(0, SECONDARY_TRAITS.length - 1)]);
        builder.flaw(FLAWS[RandomUtils.rollRange(0, FLAWS.length - 1)]);
        builder.motivation(MOTIVATIONS[RandomUtils.rollRange(0, MOTIVATIONS.length - 1)]);

        // Social class - use provided or random weighted
        SocialClass selectedClass = socialClass != null ? socialClass : selectWeightedSocialClass();
        builder.socialClass(selectedClass);

        // Background - influenced by social class
        builder.occupation(selectOccupationForClass(selectedClass));
        builder.education(selectEducationForClass(selectedClass));

        // Flavor
        builder.quirk(QUIRKS[RandomUtils.rollRange(0, QUIRKS.length - 1)]);
        builder.hobby(HOBBIES[RandomUtils.rollRange(0, HOBBIES.length - 1)]);
        builder.fear(FEARS[RandomUtils.rollRange(0, FEARS.length - 1)]);

        return builder.build();
    }

    public Person createPersonInClassRange(SocialClass minClass, SocialClass maxClass) {
        SocialClass selectedClass = SocialClass.randomInRange(minClass, maxClass);
        return createPerson(selectedClass);
    }

    public Person createPersonInClassRange(String gender, String origin, String ageRange,
                                           SocialClass minClass, SocialClass maxClass) {
        SocialClass selectedClass = SocialClass.randomInRange(minClass, maxClass);
        return createPerson(gender, origin, ageRange, selectedClass);
    }

    public Person createPersonInClassRangeWeighted(SocialClass minClass, SocialClass maxClass) {
        SocialClass selectedClass = SocialClass.weightedRandomInRange(minClass, maxClass);
        return createPerson(selectedClass);
    }

    public Person createPersonAtLeastClass(SocialClass minClass) {
        return createPersonInClassRange(minClass, SocialClass.NOBILITY);
    }

    public Person createPersonAtMostClass(SocialClass maxClass) {
        return createPersonInClassRange(SocialClass.LOWER_CLASS, maxClass);
    }

    public List<Person> createPeopleInClassRange(SocialClass minClass, SocialClass maxClass, int count) {
        List<Person> people = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            people.add(createPersonInClassRange(minClass, maxClass));
        }
        return people;
    }

    private String selectRandomOrigin() {
        if (cachedOrigins == null || cachedOrigins.isEmpty()) {
            return "Unknown";
        }
        return cachedOrigins.get(RandomUtils.rollRange(0, cachedOrigins.size() - 1)).getName();
    }

    private NameRef selectFirstName(String gender, String origin) {
        // Try to find names matching both gender and origin
        List<NameRef> candidates = nameRefRepository.findFirstNamesByGenderAndOrigin(gender, origin);

        // Fall back to just gender if no origin match
        if (candidates == null || candidates.isEmpty()) {
            candidates = nameRefRepository.findFirstNamesByGender(gender);
        }

        // Final fallback to all first names
        if (candidates == null || candidates.isEmpty()) {
            candidates = cachedFirstNames;
        }

        return selectWeightedName(candidates);
    }

    private NameRef selectLastName(String origin) {
        List<NameRef> candidates = nameRefRepository.findLastNamesByOrigin(origin);

        if (candidates == null || candidates.isEmpty()) {
            candidates = cachedLastNames;
        }

        return selectWeightedName(candidates);
    }

    private NameRef selectWeightedName(List<NameRef> names) {
        if (names == null || names.isEmpty()) {
            // Emergency fallback
            NameRef fallback = new NameRef();
            fallback.setName("Unknown");
            fallback.setPopularity(1);
            return fallback;
        }

        int totalWeight = names.stream()
                .mapToInt(NameRef::getPopularity)
                .sum();

        int random = RandomUtils.rollRange(1, totalWeight);
        int currentWeight = 0;

        for (NameRef name : names) {
            currentWeight += name.getPopularity();
            if (random <= currentWeight) {
                return name;
            }
        }

        return names.getFirst();
    }

    private int[] generateAge(String ageRange) {
        if (ageRange == null) {
            int roll = RandomUtils.rollD100();
            if (roll <= 5) ageRange = "child";
            else if (roll <= 20) ageRange = "young";
            else if (roll <= 60) ageRange = "adult";
            else if (roll <= 85) ageRange = "middle";
            else ageRange = "elder";
        }

        return switch (ageRange.toLowerCase()) {
            case "child" -> new int[]{RandomUtils.rollRange(8, 15)};
            case "young" -> new int[]{RandomUtils.rollRange(16, 25)};
            case "adult" -> new int[]{RandomUtils.rollRange(26, 40)};
            case "middle" -> new int[]{RandomUtils.rollRange(41, 60)};
            case "elder" -> new int[]{RandomUtils.rollRange(61, 85)};
            default -> new int[]{RandomUtils.rollRange(18, 65)};
        };
    }

    private String getAgeCategory(int age) {
        if (age < 16) return "child";
        if (age < 26) return "young adult";
        if (age < 41) return "adult";
        if (age < 61) return "middle-aged";
        return "elder";
    }

    private String selectHairColor(int age) {
        if (age >= 60 && RandomUtils.rollD100() <= 60) {
            return new String[]{"Gray", "White", "Salt-and-pepper"}[RandomUtils.rollRange(0, 2)];
        }
        if (age >= 45 && RandomUtils.rollD100() <= 30) {
            return "Salt-and-pepper";
        }
        // Remove gray options for younger people
        return HAIR_COLORS[RandomUtils.rollRange(0, HAIR_COLORS.length - 4)];
    }

    private int generateHeight(String gender) {
        // Using approximate human averages with variation
        if ("male".equals(gender)) {
            return RandomUtils.rollRange(160, 195); // ~5'3" to ~6'5"
        } else {
            return RandomUtils.rollRange(150, 180); // ~4'11" to ~5'11"
        }
    }

    private String describeHeight(int heightCm, String gender) {
        int avg = "male".equals(gender) ? 175 : 162;
        int diff = heightCm - avg;

        if (diff < -10) return "short";
        if (diff < -5) return "below average height";
        if (diff <= 5) return "average height";
        if (diff <= 10) return "above average height";
        return "tall";
    }

    private SocialClass selectWeightedSocialClass() {
        int roll = RandomUtils.rollD100();
        if (roll <= 15) return SocialClass.LOWER_CLASS;      // 15%
        if (roll <= 35) return SocialClass.WORKING_CLASS;    // 20%
        if (roll <= 65) return SocialClass.MIDDLE_CLASS;     // 30%
        if (roll <= 85) return SocialClass.UPPER_MIDDLE_CLASS; // 20%
        if (roll <= 97) return SocialClass.UPPER_CLASS;      // 12%
        return SocialClass.NOBILITY;                          // 3%
    }

    private Person createRandomMixedHeritagePerson(String gender, String ageRange, SocialClass socialClass) {
        String origin1 = selectRandomOrigin();
        String origin2 = selectRandomOrigin();

        int attempts = 0;
        while (origin1.equals(origin2) && attempts < 10) {
            origin2 = selectRandomOrigin();
            attempts++;
        }

        return createMixedHeritagePerson(gender, origin1, origin2, ageRange, socialClass);
    }

    public Person createMixedHeritagePerson(String gender, String maternalOrigin, String paternalOrigin,
                                            String ageRange, SocialClass socialClass) {
        Person.PersonBuilder builder = Person.builder();

        String selectedGender = gender != null ? gender : (RandomUtils.rollD100() <= 50 ? "male" : "female");
        builder.gender(selectedGender);

        builder.maternalOrigin(maternalOrigin);
        builder.paternalOrigin(paternalOrigin);
        builder.isMixedHeritage(true);
        builder.culturalOrigin(maternalOrigin + "/" + paternalOrigin);

        String firstNameOrigin = RandomUtils.rollD100() <= 50 ? maternalOrigin : paternalOrigin;
        NameRef firstName = selectFirstName(selectedGender, firstNameOrigin);

        if (firstName.getName().equals("Unknown")) {
            firstNameOrigin = firstNameOrigin.equals(maternalOrigin) ? paternalOrigin : maternalOrigin;
            firstName = selectFirstName(selectedGender, firstNameOrigin);
        }

        NameRef lastName = selectLastName(paternalOrigin);

        boolean useHyphenatedSurname = RandomUtils.rollD100() <= 20;
        if (useHyphenatedSurname) {
            NameRef maternalLastName = selectLastName(maternalOrigin);
            if (!maternalLastName.getName().equals("Unknown") && !lastName.getName().equals("Unknown")) {
                builder.lastName(maternalLastName.getName() + "-" + lastName.getName());
                builder.fullName(firstName.getName() + " " + maternalLastName.getName() + "-" + lastName.getName());
            } else {
                builder.lastName(lastName.getName());
                builder.fullName(firstName.getName() + " " + lastName.getName());
            }
        } else {
            builder.lastName(lastName.getName());
            builder.fullName(firstName.getName() + " " + lastName.getName());
        }

        builder.firstName(firstName.getName());

        if (RandomUtils.rollD100() <= 20) {
            String[] nicknames = "male".equals(selectedGender) ? NICKNAMES_MALE : NICKNAMES_FEMALE;
            builder.nickname(nicknames[RandomUtils.rollRange(0, nicknames.length - 1)]);
        }

        int[] ageData = generateAge(ageRange);
        builder.age(ageData[0]);
        builder.ageCategory(getAgeCategory(ageData[0]));

        builder.bodyType(BODY_TYPES[RandomUtils.rollRange(0, BODY_TYPES.length - 1)]);
        builder.hairColor(selectHairColor(ageData[0]));
        builder.eyeColor(EYE_COLORS[RandomUtils.rollRange(0, EYE_COLORS.length - 1)]);
        builder.distinguishingFeature(DISTINGUISHING_FEATURES[RandomUtils.rollRange(0, DISTINGUISHING_FEATURES.length - 1)]);

        int height = generateHeight(selectedGender);
        builder.heightCm(height);
        builder.heightDescription(describeHeight(height, selectedGender));

        builder.primaryTrait(PRIMARY_TRAITS[RandomUtils.rollRange(0, PRIMARY_TRAITS.length - 1)]);
        builder.secondaryTrait(SECONDARY_TRAITS[RandomUtils.rollRange(0, SECONDARY_TRAITS.length - 1)]);
        builder.flaw(FLAWS[RandomUtils.rollRange(0, FLAWS.length - 1)]);
        builder.motivation(MOTIVATIONS[RandomUtils.rollRange(0, MOTIVATIONS.length - 1)]);

        // Social class - use provided or random weighted
        SocialClass selectedClass = socialClass != null ? socialClass : selectWeightedSocialClass();
        builder.socialClass(selectedClass);
        builder.occupation(selectOccupationForClass(selectedClass));
        builder.education(selectEducationForClass(selectedClass));

        builder.quirk(QUIRKS[RandomUtils.rollRange(0, QUIRKS.length - 1)]);
        builder.hobby(HOBBIES[RandomUtils.rollRange(0, HOBBIES.length - 1)]);
        builder.fear(FEARS[RandomUtils.rollRange(0, FEARS.length - 1)]);

        return builder.build();
    }

    public Person createChild(Person parent1, Person parent2, String gender, Integer age) {
        Person.PersonBuilder builder = Person.builder();

        // Gender
        String selectedGender = gender != null ? gender : (RandomUtils.rollD100() <= 50 ? "male" : "female");
        builder.gender(selectedGender);

        // Determine the child's origins from parents
        String maternalOrigin = getEffectiveOrigin(parent1, true);  // true = maternal side
        String paternalOrigin = getEffectiveOrigin(parent2, false); // false = paternal side

        builder.maternalOrigin(maternalOrigin);
        builder.paternalOrigin(paternalOrigin);

        // Check if parents have different origins
        boolean parentsDifferentOrigins = !maternalOrigin.equals(paternalOrigin);
        builder.isMixedHeritage(parentsDifferentOrigins);

        if (parentsDifferentOrigins) {
            builder.culturalOrigin(maternalOrigin + "/" + paternalOrigin);
        } else {
            builder.culturalOrigin(maternalOrigin);
        }

        // First name: from either parent's culture
        String firstNameOrigin = RandomUtils.rollD100() <= 50 ? maternalOrigin : paternalOrigin;
        NameRef firstName = selectFirstName(selectedGender, firstNameOrigin);

        // Fallback to other parent's origin if no match
        if (firstName.getName().equals("Unknown")) {
            firstNameOrigin = firstNameOrigin.equals(maternalOrigin) ? paternalOrigin : maternalOrigin;
            firstName = selectFirstName(selectedGender, firstNameOrigin);
        }

        // Last name: typically from parent2, but some options
        String childLastName = determineChildLastName(parent1, parent2);

        builder.firstName(firstName.getName());
        builder.lastName(childLastName);
        builder.fullName(firstName.getName() + " " + childLastName);

        // Nickname: small chance, maybe family nickname
        if (RandomUtils.rollD100() <= 10) {
            String[] nicknames = "male".equals(selectedGender) ? NICKNAMES_MALE : NICKNAMES_FEMALE;
            builder.nickname(nicknames[RandomUtils.rollRange(0, nicknames.length - 1)]);
        }

        // Age: child range (0-17) unless specified
        int childAge = age != null ? age : RandomUtils.rollRange(0, 17);
        builder.age(childAge);
        builder.ageCategory(getAgeCategory(childAge));

        // Physical attributes - inherited with variation
        builder.bodyType(inheritBodyType(parent1, parent2));
        builder.hairColor(inheritHairColor(parent1, parent2));
        builder.eyeColor(inheritEyeColor(parent1, parent2));
        builder.heightCm(inheritHeight(parent1, parent2, selectedGender, childAge));
        builder.heightDescription(describeHeight(builder.build().getHeightCm(), selectedGender));

        // Distinguishing features - small chance to inherit parent's
        builder.distinguishingFeature(inheritDistinguishingFeature(parent1, parent2));

        // Personality - mix of inherited tendencies and unique traits
        builder.primaryTrait(inheritOrRandomTrait(parent1.getPrimaryTrait(), parent2.getPrimaryTrait(), PRIMARY_TRAITS));
        builder.secondaryTrait(SECONDARY_TRAITS[RandomUtils.rollRange(0, SECONDARY_TRAITS.length - 1)]);
        builder.flaw(FLAWS[RandomUtils.rollRange(0, FLAWS.length - 1)]);
        builder.motivation(selectChildMotivation(childAge));

        // Background - age appropriate
        builder.occupation(childAge < 16 ? "Student" : OCCUPATIONS[RandomUtils.rollRange(0, OCCUPATIONS.length - 1)]);
        SocialClass familyClass = determineChildSocialClass(parent1.getSocialClass(), parent2.getSocialClass());
        builder.socialClass(familyClass);

        // Background - age appropriate
        if (age != null && age < 16) {
            builder.occupation("Student");
        } else {
            builder.occupation(selectOccupationForClass(familyClass));
        }

        builder.education(age != null && age < 6 ? "None" : selectEducationForClass(familyClass));


        // Flavor
        builder.quirk(QUIRKS[RandomUtils.rollRange(0, QUIRKS.length - 1)]);
        builder.hobby(inheritOrRandomHobby(parent1.getHobby(), parent2.getHobby()));
        builder.fear(FEARS[RandomUtils.rollRange(0, FEARS.length - 1)]);

        return builder.build();
    }

    private SocialClass determineChildSocialClass(SocialClass parent1Class, SocialClass parent2Class) {
        if (RandomUtils.rollD100() <= 50) {
            return parent1Class;
        } else {
            return parent2Class;
        }
    }

    public Person createChild(Person parent1, Person parent2) {
        return createChild(parent1, parent2, null, null);
    }

    public List<Person> createChildren(Person parent1, Person parent2, int count) {
        List<Person> children = new ArrayList<>();

        // Generate ages that make sense (spread them out)
        int baseAge = RandomUtils.rollRange(2, 10);

        for (int i = 0; i < count; i++) {
            int childAge = Math.max(0, baseAge + RandomUtils.rollRange(-2, 3) - (i * 2));
            children.add(createChild(parent1, parent2, null, childAge));
        }

        return children;
    }

    private String getEffectiveOrigin(Person parent, boolean maternalSide) {
        if (parent.isMixedHeritage()) {
            // If parent is mixed, randomly pick one of their origins to pass down
            // Or use their maternal/paternal based on which side we're getting
            if (maternalSide && parent.getMaternalOrigin() != null) {
                return RandomUtils.rollD100() <= 50 ? parent.getMaternalOrigin() : parent.getPaternalOrigin();
            } else if (parent.getPaternalOrigin() != null) {
                return RandomUtils.rollD100() <= 50 ? parent.getPaternalOrigin() : parent.getMaternalOrigin();
            }
        }
        return parent.getCulturalOrigin();
    }

    private String determineChildLastName(Person parent1, Person parent2) {
        int roll = RandomUtils.rollD100();

        if (roll <= 70) {
            // 70% - parent2's surname only
            return parent2.getLastName();
        } else if (roll <= 85) {
            // 15% - Hyphenated (parent1-parent2)
            return parent1.getLastName() + "-" + parent2.getLastName();
        } else if (roll <= 95) {
            // 10% - parent1's surname only
            return parent1.getLastName();
        } else {
            // 5% - Hyphenated (parent2-parent1)
            return parent2.getLastName() + "-" + parent1.getLastName();
        }
    }

    private String inheritBodyType(Person parent1, Person parent2) {
        int roll = RandomUtils.rollD100();

        if (roll <= 35) {
            return parent1.getBodyType();
        } else if (roll <= 70) {
            return parent2.getBodyType();
        } else {
            // 30% chance of different body type
            return BODY_TYPES[RandomUtils.rollRange(0, BODY_TYPES.length - 1)];
        }
    }

    private String inheritHairColor(Person parent1, Person parent2) {

        String[] parentHairs = {parent1.getHairColor(), parent2.getHairColor()};
        List<String> validColors = new ArrayList<>();
        for (String color : parentHairs) {
            if (color != null && !color.contains("Gray") && !color.contains("White") && !color.contains("Salt")) {
                validColors.add(color);
            }
        }

        if (validColors.isEmpty()) {
            return HAIR_COLORS[RandomUtils.rollRange(0, 8)];
        }

        int roll = RandomUtils.rollD100();
        if (roll <= 40) {
            return validColors.getFirst();
        } else if (roll <= 80 && validColors.size() > 1) {
            return validColors.get(1);
        } else if (roll <= 90 && validColors.size() > 1) {
            // Blend - darker usually dominates
            return getDarkerHairColor(validColors.get(0), validColors.get(1));
        } else {
            // Random mutation
            return HAIR_COLORS[RandomUtils.rollRange(0, 8)];
        }
    }

    private String getDarkerHairColor(String color1, String color2) {
        // Simplified: darker colors tend to dominate
        String[] darkOrder = {"Black", "Dark brown", "Brown", "Auburn", "Red", "Light brown", "Strawberry blonde", "Blonde"};

        int idx1 = 99, idx2 = 99;
        for (int i = 0; i < darkOrder.length; i++) {
            if (color1.contains(darkOrder[i])) idx1 = i;
            if (color2.contains(darkOrder[i])) idx2 = i;
        }

        return idx1 <= idx2 ? color1 : color2;
    }

    private String inheritEyeColor(Person parent1, Person parent2) {
        int roll = RandomUtils.rollD100();

        if (roll <= 40) {
            return parent1.getEyeColor();
        } else if (roll <= 80) {
            return parent2.getEyeColor();
        } else {
            // 20% recessive or variation
            return EYE_COLORS[RandomUtils.rollRange(0, EYE_COLORS.length - 1)];
        }
    }

    private int inheritHeight(Person parent1, Person parent2, String gender, int age) {
        // Adult height prediction based on parents
        int parent1Height = parent1.getHeightCm();
        int parent2Height = parent2.getHeightCm();

        // Mid-parent height method (simplified)
        int predictedAdultHeight;
        if ("male".equals(gender)) {
            predictedAdultHeight = (int) ((parent1Height + parent2Height + 13) / 2.0);
        } else {
            predictedAdultHeight = (int) ((parent1Height + parent2Height - 13) / 2.0);
        }

        // Add some random variation (+/- 8cm)
        predictedAdultHeight += RandomUtils.rollRange(-8, 8);

        // Scale by age (rough growth curve)
        if (age >= 18) {
            return predictedAdultHeight;
        } else if (age >= 13) {
            return (int) (predictedAdultHeight * (0.85 + (age - 13) * 0.03));
        } else if (age >= 6) {
            return (int) (predictedAdultHeight * (0.65 + (age - 6) * 0.03));
        } else if (age >= 2) {
            return (int) (predictedAdultHeight * (0.45 + (age - 2) * 0.05));
        } else {
            return (int) (predictedAdultHeight * 0.35);
        }
    }

    private String inheritDistinguishingFeature(Person parent1, Person parent2) {
        int roll = RandomUtils.rollD100();

        // Heritable features
        String[] heritableFeatures = {"freckles", "dimples", "birthmark"};

        if (roll <= 15 && parent1.getDistinguishingFeature() != null) {
            for (String heritable : heritableFeatures) {
                if (parent1.getDistinguishingFeature().contains(heritable)) {
                    return parent1.getDistinguishingFeature();
                }
            }
        } else if (roll <= 30 && parent2.getDistinguishingFeature() != null) {
            for (String heritable : heritableFeatures) {
                if (parent2.getDistinguishingFeature().contains(heritable)) {
                    return parent2.getDistinguishingFeature();
                }
            }
        }

        // Otherwise random or none (kids less likely to have scars, weathered skin, etc.)
        String[] childAppropriateFeatures = {"numerous freckles", "dimples", "a birthmark", "a warm smile", null, null, null};
        return childAppropriateFeatures[RandomUtils.rollRange(0, childAppropriateFeatures.length - 1)];
    }

    private String inheritOrRandomTrait(String parent1Trait, String parent2Trait, String[] allTraits) {
        int roll = RandomUtils.rollD100();

        if (roll <= 25 && parent1Trait != null) {
            return parent1Trait;
        } else if (roll <= 50 && parent2Trait != null) {
            return parent2Trait;
        } else {
            return allTraits[RandomUtils.rollRange(0, allTraits.length - 1)];
        }
    }

    private String selectChildMotivation(int age) {
        if (age < 6) {
            String[] toddlerMotivations = {"Seeking comfort", "Exploring the world", "Finding belonging", "Gaining approval"};
            return toddlerMotivations[RandomUtils.rollRange(0, toddlerMotivations.length - 1)];
        } else if (age < 13) {
            String[] childMotivations = {"Making friends", "Proving themselves", "Gaining approval", "Pursuing knowledge", "Finding belonging"};
            return childMotivations[RandomUtils.rollRange(0, childMotivations.length - 1)];
        } else {
            String[] teenMotivations = {"Proving themselves", "Seeking freedom", "Finding love", "Achieving fame", "Pursuing knowledge", "Finding belonging"};
            return teenMotivations[RandomUtils.rollRange(0, teenMotivations.length - 1)];
        }
    }

    private String inheritOrRandomHobby(String parent1Hobby, String parent2Hobby) {
        int roll = RandomUtils.rollD100();

        if (roll <= 20 && parent1Hobby != null) {
            return parent1Hobby;
        } else if (roll <= 40 && parent2Hobby != null) {
            return parent2Hobby;
        } else {
            return HOBBIES[RandomUtils.rollRange(0, HOBBIES.length - 1)];
        }
    }

    private String selectOccupationForClass(SocialClass socialClass) {
        String[] occupations = switch (socialClass) {
            case LOWER_CLASS -> LOWER_CLASS_OCCUPATIONS;
            case WORKING_CLASS -> WORKING_CLASS_OCCUPATIONS;
            case MIDDLE_CLASS -> MIDDLE_CLASS_OCCUPATIONS;
            case UPPER_MIDDLE_CLASS -> UPPER_MIDDLE_CLASS_OCCUPATIONS;
            case UPPER_CLASS -> UPPER_CLASS_OCCUPATIONS;
            case NOBILITY -> NOBILITY_OCCUPATIONS;
        };
        return occupations[RandomUtils.rollRange(0, occupations.length - 1)];
    }

    private String selectEducationForClass(SocialClass socialClass) {
        String[] educations = switch (socialClass) {
            case LOWER_CLASS -> LOWER_CLASS_EDUCATION;
            case WORKING_CLASS -> WORKING_CLASS_EDUCATION;
            case MIDDLE_CLASS -> MIDDLE_CLASS_EDUCATION;
            case UPPER_MIDDLE_CLASS -> UPPER_MIDDLE_CLASS_EDUCATION;
            case UPPER_CLASS -> UPPER_CLASS_EDUCATION;
            case NOBILITY -> NOBILITY_EDUCATION;
        };
        return educations[RandomUtils.rollRange(0, educations.length - 1)];
    }

    public List<Person> createPeopleOfClass(SocialClass socialClass, int count) {
        List<Person> people = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            people.add(createPerson(socialClass));
        }
        return people;
    }

    public List<Person> createPopulation(int count) {
        List<Person> population = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            population.add(createPerson()); // Uses weighted distribution
        }

        return population;
    }

    public List<Person> createPopulationWithDistribution(int count, Map<SocialClass, Integer> distribution) {
        List<Person> population = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            SocialClass selectedClass = selectClassFromDistribution(distribution);
            population.add(createPerson(selectedClass));
        }

        return population;
    }

    private SocialClass selectClassFromDistribution(Map<SocialClass, Integer> distribution) {
        int roll = RandomUtils.rollD100();
        int cumulative = 0;

        for (Map.Entry<SocialClass, Integer> entry : distribution.entrySet()) {
            cumulative += entry.getValue();
            if (roll <= cumulative) {
                return entry.getKey();
            }
        }

        return SocialClass.MIDDLE_CLASS; // fallback
    }

    public static final Comparator<Person> BY_CLASS_ASCENDING =
            Comparator.comparing(p -> p.getSocialClass().getRank());

    public static final Comparator<Person> BY_CLASS_DESCENDING =
            Comparator.comparing(p -> p.getSocialClass().getRank(), Comparator.reverseOrder());


    @Getter
    public enum SocialClass {
        LOWER_CLASS("Lower class", 0),
        WORKING_CLASS("Working class", 1),
        MIDDLE_CLASS("Middle class", 2),
        UPPER_MIDDLE_CLASS("Upper middle class", 3),
        UPPER_CLASS("Upper class", 4),
        NOBILITY("Nobility", 5);

        private final String displayName;
        private final int rank;

        SocialClass(String displayName, int rank) {
            this.displayName = displayName;
            this.rank = rank;
        }

        public static SocialClass fromRank(int rank) {
            for (SocialClass sc : values()) {
                if (sc.rank == rank) {
                    return sc;
                }
            }
            return MIDDLE_CLASS; // default
        }

        public static SocialClass fromDisplayName(String displayName) {
            for (SocialClass sc : values()) {
                if (sc.displayName.equalsIgnoreCase(displayName)) {
                    return sc;
                }
            }
            return MIDDLE_CLASS; // default
        }

        public boolean isHigherThan(SocialClass other) {
            return this.rank > other.rank;
        }

        public boolean isLowerThan(SocialClass other) {
            return this.rank < other.rank;
        }

        public int rankDifference(SocialClass other) {
            return this.rank - other.rank;
        }

        public SocialClass oneRankHigher() {
            return fromRank(Math.min(this.rank + 1, NOBILITY.rank));
        }

        public SocialClass oneRankLower() {
            return fromRank(Math.max(this.rank - 1, LOWER_CLASS.rank));
        }

        public boolean isWithinRange(SocialClass min, SocialClass max) {
            return this.rank >= min.rank && this.rank <= max.rank;
        }

        public static SocialClass randomInRange(SocialClass min, SocialClass max) {
            int minRank = Math.min(min.rank, max.rank);
            int maxRank = Math.max(min.rank, max.rank);
            int randomRank = RandomUtils.rollRange(minRank, maxRank);
            return fromRank(randomRank);
        }

        public static SocialClass weightedRandomInRange(SocialClass min, SocialClass max) {
            int minRank = Math.min(min.rank, max.rank);
            int maxRank = Math.max(min.rank, max.rank);

            // Use bell curve - roll twice and average for middle-weighted distribution
            int roll1 = RandomUtils.rollRange(minRank, maxRank);
            int roll2 = RandomUtils.rollRange(minRank, maxRank);
            int randomRank = (roll1 + roll2) / 2;

            return fromRank(randomRank);
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

}
