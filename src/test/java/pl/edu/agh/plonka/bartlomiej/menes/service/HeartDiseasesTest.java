package pl.edu.agh.plonka.bartlomiej.menes.service;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.edu.agh.plonka.bartlomiej.menes.model.ObjectProperty;
import pl.edu.agh.plonka.bartlomiej.menes.model.Patient;

import java.io.File;
import java.util.*;

import static java.lang.System.currentTimeMillis;
import static java.util.stream.Collectors.toSet;

public class HeartDiseasesTest {

    private static final Random RAND = new Random(currentTimeMillis());
    private static OntologyWrapper ontology;
    private static MachineLearning machineLearning;
    private static PatientsService service;
    private static Set<Patient> patients;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ontology = new OntologyWrapper(new File("src/test/resources/heart_disease.rdf"));
        machineLearning = new MachineLearning(ontology);
        service = new PatientsService(ontology, machineLearning);
        patients = service.getPatients();
    }

    @Before
    public void setUp() throws Exception {
        service.deleteAllRules();
        service.deleteAllPatients();
        service.addPatients(patients);
        service.saveKnowledgeBase(new File("src/test/resources/heart_disease2.rdf"));
    }

    @Test
    public void test() throws Throwable {
//        Set<ObjectProperty> categories = ontology.getEntityProperties()
//                .stream()
//                .filter(property -> property.getID().equals("ill"))
//                .collect(toSet());
//        MachineLearningInput<Patient> input = prepareMachineLearningInput(2, categories);
//        service.addPatients(input.trainingSet);
//        service.learnNewRules(categories);
//        service.addPatients(input.testSet.stream().map(TestPatient::getPatient).collect(toSet()));
//        service.infer();
//        long invalidInferences = input.testSet.stream().filter(TestPatient::invalidPatient).count();
//        assertEquals(0, invalidInferences);
    }

    private MachineLearningInput<Patient> prepareMachineLearningInput(int trainingCount, Set<ObjectProperty> categories) {
        Set<Patient> trainingSet = selectRandomSubset(patients, trainingCount);
        Set<TestPatient> testSet = removeElements(patients, trainingSet)
                .stream()
                .map(patient -> new TestPatient(patient, categories))
                .collect(toSet());
        return new MachineLearningInput<>(trainingSet, testSet);
    }

    private <T> Set<T> subSet(Set<T> set, int count) {
        Iterator<T> iterator = set.iterator();
        HashSet<T> result = new HashSet<>();
        while (count > 0 && iterator.hasNext()) {
            result.add(iterator.next());
            count--;
        }
        return result;
    }

    private <T> Set<T> selectRandomSubset(Collection<T> set, int size) {
        List<T> wrappingSet = new ArrayList<T>(set);
        Set<T> subset = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            int nextIndex = RAND.nextInt(wrappingSet.size());
            subset.add(wrappingSet.remove(nextIndex));
        }
        return subset;
    }

    private <T> Set<T> removeElements(Collection<T> elements, Collection<T> elementsToRemove) {
        Set<T> result = new HashSet<>(elements);
        result.removeAll(elementsToRemove);
        return result;
    }

}
