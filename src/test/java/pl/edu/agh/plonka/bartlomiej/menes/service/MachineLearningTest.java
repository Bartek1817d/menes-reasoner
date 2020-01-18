package pl.edu.agh.plonka.bartlomiej.menes.service;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.util.Assert;
import pl.edu.agh.plonka.bartlomiej.menes.model.*;
import pl.edu.agh.plonka.bartlomiej.menes.model.rule.Rule;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.stream;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;
import static pl.edu.agh.plonka.bartlomiej.menes.utils.Others.findEntity;

public class MachineLearningTest {

    private static final Logger LOG = getLogger(MachineLearningTest.class);

    private static final Random RAND = new Random(currentTimeMillis());
    private static final Boolean MOCK_ONTOLOGY = false;
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    private static final String[] DISEASES = {"Cold", "LungCancer", "Chickenpox", "Myocarditis", "Pericarditis"};
    private static final String[] SYMPTOMS = {"Cough", "StabbingChestPain", "Dyspnoea"};
    private static final String[] TESTS = {"EKG", "ChestXRay"};
//    private static final String CLASSES = "Patient";

    private static final Set<Entity> ENTITIES = mockEntities();
    private static final Set<OntologyClass> CLASSES = mockClasses();

    private static final Set<Property> STRING_PROPERTIES = mockStringProperties();
    private static final Set<IntegerProperty> INTEGER_PROPERTIES = mockIntegerProperties();
    private static final Set<ObjectProperty> OBJECT_PROPERTIES = mockObjectProperties();

    private static final Set<Patient> PATIENTS = mockPatients();

    private static Set<Patient> mockPatients() {
        Patient patient1 = new Patient("patient1");
        patient1
    }

    private static Set<Entity> mockEntities() {
        Set<Entity> entities = new HashSet<>();
        entities.addAll(mockEntities(DISEASES));
        entities.addAll(mockEntities(SYMPTOMS));
        entities.addAll(mockEntities(TESTS));
        return entities;
    }

    private static Set<Entity> mockEntities(String... entities) {
        return stream(entities).map(Entity::new).collect(toSet());
    }

    private static Set<OntologyClass> mockClasses() {
        Set<OntologyClass> ontologyClasses = new HashSet<>();
        ontologyClasses.add(mockClass("Disease", DISEASES));
        ontologyClasses.add(mockClass("Symptom", SYMPTOMS));
        ontologyClasses.add(mockClass("Test", TESTS));
        return ontologyClasses;
    }

    private static OntologyClass mockClass(String name, String[] entities) {
        OntologyClass ontologyClass = new OntologyClass(name);
        ontologyClass.addInstances(stream(entities).map(e -> findEntity(e, ENTITIES)).collect(toSet()));
        return ontologyClass;
    }

    private static Set<Property> mockStringProperties() {
        Set<Property> properties = new HashSet<>();
        properties.add(new Property("firstName"));
        properties.add(new Property("lastName"));
        return properties;
    }

    private static Set<IntegerProperty> mockIntegerProperties() {
        Set<IntegerProperty> properties = new HashSet<>();
        properties.add(new IntegerProperty("age", 0, 100));
        properties.add(new IntegerProperty("height", 50, 200));
        properties.add(new IntegerProperty("weight", 10, 200));
        return properties;
    }

    private static Set<ObjectProperty> mockObjectProperties() {
        Set<ObjectProperty> properties = new HashSet<>();
        properties.add(new ObjectProperty("hasDisease", singleton(findEntity("Disease", CLASSES))));
        properties.add(new ObjectProperty("hasSymptom", singleton(findEntity("Symptom", CLASSES))));
        properties.add(new ObjectProperty("shouldMakeTest", singleton(findEntity("Test", CLASSES))));
        properties.add(new ObjectProperty("negativeTest", singleton(findEntity("Test", CLASSES))));

        return properties;
    }


    private static OntologyWrapper ontology;

    private static MachineLearning machineLearning;

    @BeforeClass
    public static void setUp() throws Exception {
        if (MOCK_ONTOLOGY) {
            mockOntology();
        } else {
            ontology = new OntologyWrapper(new File("src/test/resources/human_diseases.owl"));
        }
        machineLearning = new MachineLearning(ontology);
    }

    @Test
    public void name() throws Throwable {
        Set<Patient> patients = ontology.getPatients();
        Set<ObjectProperty> hasDiseaseProperty = ontology.getEntityProperties()
                .stream()
                .filter(p -> p.getID().equals("hasDisease"))
                .collect(toSet());

        Collection<Rule> rules = machineLearning.sequentialCovering(patients, hasDiseaseProperty);

        Assert.notEmpty(rules);
    }

    private static void mockOntology() {
        ontology = mock(OntologyWrapper.class);
        when(ontology.getClasses()).thenReturn(CLASSES);
        when(ontology.getStringProperties()).thenReturn(STRING_PROPERTIES);
        when(ontology.getIntegerProperties()).thenReturn(INTEGER_PROPERTIES);
        when(ontology.getEntityProperties()).thenReturn(OBJECT_PROPERTIES);
    }

    //    @Test
//    public void testNumericalComplexity() throws Throwable {
//        PrintWriter results = new PrintWriter(new FileOutputStream(
//                new File(format("src/test/resources/result_%s.csv", TIMESTAMP_FORMAT.format(new Date())))));
//        results.println("n,time");
//        int maxN = 50;
//        for (int n = 1; n <= maxN; n++) {
//            LOG.info(Integer.toString(n));
//            Set<Patient> patients = generatePatients(n);
//            long start = nanoTime();
//            machineLearning.sequentialCovering(patients);
//            long stop = nanoTime();
//            results.println(format(Locale.US, "%d,%f", n, (float) (stop - start) / 1000000000));
//        }
//        results.close();
//    }

    @Test
    public void testGeneratingRules() throws Throwable {
        Set<Patient> patients = new HashSet<>();
        patients.add(generatePatient("patient1", 24, "StabbingChestPain", "EKG", "Myocarditis"));
        patients.add(generatePatient("patient2", 24, "Dyspnoea", "ChestXRay", "Pericarditis"));
        patients.add(generatePatient("patient3", 60, "StabbingChestPain", "ChestXRay", "LungCancer"));

        HashSet<ObjectProperty> predicateCategories = new HashSet<>();

        Collection<Rule> rules = machineLearning.sequentialCovering(patients);
        assertEquals(3, rules.size());
    }


    private Patient generatePatient(String patientId, Integer age, String symptom, String negativeTest, String disease) {
        Patient patient = new Patient(patientId);
        patient.setIntegerProperty("age", age);
        patient.setEntityProperty("hasSymptom", findEntity(symptom, ENTITIES));
        patient.setEntityProperty("negativeTest", findEntity(negativeTest, ENTITIES));
        patient.setEntityProperty("hasDisease", findEntity(disease, ENTITIES));
        return patient;
    }
//
//    private Set<Patient> generatePatients(int count) {
//        Set<Patient> patients = new HashSet<>(count);
//        for (int i = 0; i < count; i++) {
//            patients.add(generatePatient("patient" + (i + 1)));
//        }
//        return patients;
//    }
//
//    private Patient generatePatient(String id) {
//        Patient patient = new Patient(id);
//        patient.setAge(PATIENT_MIN_AGE + RAND.nextInt(PATIENT_MAX_AGE - PATIENT_MIN_AGE));
//        patient.setHeight(PATIENT_MIN_HEIGHT + RAND.nextInt(PATIENT_MAX_HEIGHT - PATIENT_MIN_HEIGHT));
//        patient.setWeight(PATIENT_MIN_WEIGHT + RAND.nextInt(PATIENT_MAX_WEIGHT - PATIENT_MIN_WEIGHT));
//        patient.setSymptoms(selectRandomSubset(ontology.getSymptoms().values()));
//        patient.setCauses(selectRandomSubset(ontology.getCauses().values()));
//        patient.setDiseases(selectRandomSubset(ontology.getDiseases().values()));
//        patient.setNegativeTests(selectRandomSubset(ontology.getTests().values()));
//        patient.setTreatments(selectRandomSubset(ontology.getTreatments().values()));
//        patient.setPreviousDiseases(selectRandomSubset(ontology.getDiseases().values()));
//        patient.setTests(selectRandomSubset(ontology.getTests().values()));
//        return patient;
//    }
//
//
//    private Collection<Entity> selectRandomSubset(Collection<Entity> set) {
//        List<Entity> list = new ArrayList<>(set);
//        int size = RAND.nextInt(list.size()) + 1;
//        Collection<Entity> subSet = new HashSet<>(size);
//        for (int i = 0; i < size; i++) {
//            int nextIndex = RAND.nextInt(list.size());
//            subSet.add(list.get(nextIndex));
//        }
//        return subSet;
//    }
//
//    private static void mockOntology() {
//        ontology = mock(OntologyWrapper.class);
//        when(ontology.getDiseases()).thenReturn(mockDiseases());
//        when(ontology.getSymptoms()).thenReturn(mockSymptoms());
//        when(ontology.getTests()).thenReturn(mockTests());
//        when(ontology.getTreatments()).thenReturn(mockTreatments());
//        when(ontology.getCauses()).thenReturn(mockCauses());
//        when(ontology.getClasses()).thenReturn(mockClasses());
//    }

}