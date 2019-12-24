package pl.edu.agh.plonka.bartlomiej.menes.utils;

public class Constants {

    public static final String SWRLB_PREFIX = "swrlb";

    //data properties
    public static final String AGE_PROPERTY = "age";
    public static final String FIRST_NAME_PROPERTY = "firstName";
    public static final String LAST_NAME_PROPERTY = "lastName";
    public static final String HEIGHT_PROPERTY = "height";
    public static final String WEIGHT_PROPERTY = "weight";
    public static final String EQUAL_PROPERTY = "equal";
    public static final String GREATER_THAN_PROPERTY = "greaterThan";
    public static final String GREATER_THAN_OR_EQUAL_PROPERTY = "greaterThanOrEqual";
    public static final String LESS_THAN_PROPERTY = "lessThan";
    public static final String LESS_THAN_OR_EQUAL_PROPERTY = "lessThanOrEqual";

    //object properties
    public static final String HAS_SYMPTOM_PROPERTY = "hasSymptom";
    public static final String HAS_DISEASE_PROPERTY = "hasDisease";
    public static final String NEGATIVE_TEST_PROPERTY = "negativeTest";
    public static final String PREVIOUS_DISEASE_PROPERTY = "previousDisease";
    public static final String SHOULD_MAKE_TEST_PROPERTY = "shouldMakeTest";
    public static final String SHOULD_BE_TREATED_WITH_PROPERTY = "shouldBeTreatedWith";
    public static final String CAUSE_OF_DISEASE_PROPERTY = "causeOfDisease";

    //classes
    public static final String CAUSE_CLASS = "Cause";
    public static final String DISEASE_CLASS = "Disease";
    public static final String PATIENT_CLASS = "Patient";
    public static final String PLACE_OF_RESIDENCE_CLASS = "PlaceOfResidence";
    public static final String SEASON_CLASS = "Season";
    public static final String SYMPTOM_CLASS = "Symptom";
    public static final String TESTING_CLASS = "Testing";
    public static final String TREATMENT_CLASS = "Treatment";

    public static final String GENERATED_RULE_PREFIX = "Generated";

    //constraints
    public static final int PATIENT_MIN_AGE = 0;
    public static final int PATIENT_MAX_AGE = 100;
    public static final int PATIENT_MIN_HEIGHT = 50;
    public static final int PATIENT_MAX_HEIGHT = 220;
    public static final int PATIENT_MIN_WEIGHT = 10;
    public static final int PATIENT_MAX_WEIGHT = 200;

    public static final String BUNDLE_PATH = "translation/i18n";
}
