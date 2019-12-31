package pl.edu.agh.plonka.bartlomiej.menes.service;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import pl.edu.agh.plonka.bartlomiej.menes.model.Entity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static org.slf4j.LoggerFactory.getLogger;

public class MachineLearningTest {

    private static final Logger LOG = getLogger(MachineLearningTest.class);

    private static final Random RAND = new Random(currentTimeMillis());
    private static final Boolean MOCK_ONTOLOGY = false;
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    private static final String[] DISEASES = {"Cold", "LungCancer", "Chickenpox", "Myocarditis", "Pericarditis"};
    private static final String[] SYMPTOMS = {"Cough", "StabbingChestPain", "Dyspnoea"};
    private static final String[] TESTS = {"EKG", "ChestXRay"};
    private static final String CLASSES = "Patient";

    private static OntologyWrapper ontology;
    private static MachineLearning machineLearning;

    private static Map<String, Entity> mockDiseases() {
        return mockEntities(DISEASES);
    }

    private static Map<String, Entity> mockSymptoms() {
        return mockEntities(SYMPTOMS);
    }

    private static Map<String, Entity> mockTests() {
        return mockEntities(TESTS);
    }

    private static Map<String, Entity> mockTreatments() {
        return new HashMap<>();
    }

    private static Map<String, Entity> mockCauses() {
        return new HashMap<>();
    }

    private static Map<String, Entity> mockClasses() {
        return mockEntities(CLASSES);
    }

    private static Map<String, Entity> mockEntities(String... entities) {
        return stream(entities).collect(Collectors.toMap(identity(), Entity::new));
    }

    @BeforeClass
    public static void setUp() throws Exception {
        if (MOCK_ONTOLOGY) {
//            mockOntology();
        } else {
            ontology = new OntologyWrapper(new File("src/test/resources/human_diseases.owl"));
        }
        machineLearning = new MachineLearning(ontology);
    }

    @Test
    public void name() {

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

//    @Test
//    public void testGeneratingRules() throws Throwable {
//        Set<Patient> patients = new HashSet<>();
//        patients.add(generatePatient("patient1", 24, "StabbingChestPain", "EKG", "Myocarditis"));
//        patients.add(generatePatient("patient2", 24, "Dyspnoea", "ChestXRay", "Pericarditis"));
//        patients.add(generatePatient("patient3", 60, "StabbingChestPain", "ChestXRay", "LungCancer"));
//
//        Collection<Rule> rules = machineLearning.sequentialCovering(patients);
//        assertEquals(3, rules.size());
//    }
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
//    private Patient generatePatient(String patientId, Integer age, String symptom, String negativeTest, String disease) {
//        Patient patient = new Patient(patientId);
//        patient.setAge(age);
//        patient.setSymptoms(singleton(ontology.getSymptoms().get(symptom)));
//        patient.setNegativeTests(singleton(ontology.getTests().get(negativeTest)));
//        patient.setDiseases(singleton(ontology.getDiseases().get(disease)));
//        return patient;
//    }
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