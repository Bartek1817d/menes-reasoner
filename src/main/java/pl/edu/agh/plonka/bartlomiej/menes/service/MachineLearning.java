package pl.edu.agh.plonka.bartlomiej.menes.service;

import org.slf4j.Logger;
import pl.edu.agh.plonka.bartlomiej.menes.exception.PartialStarCreationException;
import pl.edu.agh.plonka.bartlomiej.menes.model.*;
import pl.edu.agh.plonka.bartlomiej.menes.model.rule.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.symmetricDifference;
import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;
import static pl.edu.agh.plonka.bartlomiej.menes.model.rule.ComplexComparator.sortStar;
import static pl.edu.agh.plonka.bartlomiej.menes.utils.Constants.GENERATED_RULE_PREFIX;

public class MachineLearning {

    private static final Logger LOG = getLogger(MachineLearning.class);

    // 0 for restrictive, 1 for general
    private static final float epsilon = 0.5f;
    private OntologyWrapper ontology;

    public MachineLearning(OntologyWrapper ontology) {
        this.ontology = ontology;
    }

    public Collection<Rule> sequentialCovering(Set<Patient> trainingSet, Set<ObjectProperty> predicateCategories) throws Throwable {
        ExecutorService service = Executors.newCachedThreadPool();
        Collection<Callable<Collection<Rule>>> callables = prepareCallables(trainingSet, predicateCategories);
        List<Future<Collection<Rule>>> futures = service.invokeAll(callables);
        return collectResults(futures);
    }

    private Collection<Rule> collectResults(List<Future<Collection<Rule>>> futures) throws Throwable {
        Collection<Rule> rules = new HashSet<>();
        for (Future<Collection<Rule>> future : futures) {
            try {
                rules.addAll(future.get());
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        }
        return simplifyRules(rules);
    }

    private Collection<Callable<Collection<Rule>>> prepareCallables(Set<Patient> trainingSet, Set<ObjectProperty> predicateCategories) {
        Collection<Callable<Collection<Rule>>> callables = new ArrayList<>();
        PremiseProperties premiseProperties = new PremiseProperties(
                ontology.getIntegerProperties(),
                difference(ontology.getEntityProperties(), predicateCategories));
        for (ObjectProperty predicate : predicateCategories) {
            for (Entity instance : predicate.getRangeValues()) {
                callables.add(() -> sequentialCovering(trainingSet, new Category(instance, predicate.getID()), premiseProperties));
            }
        }
        return callables;
    }

    private Collection<Rule> sequentialCovering(Set<Patient> trainingSet, Category category,
                                                PremiseProperties premiseProperties) throws PartialStarCreationException {
        Collection<Rule> rules = new HashSet<>();
        Set<Patient> uncoveredSet = new HashSet<>(trainingSet);
        int ruleIdx = 1;
        while (assertPatientWithCategoryInSet(uncoveredSet, category)) {
            Complex complex = findComplex(trainingSet, uncoveredSet, category, premiseProperties);
            removeCoveredExamples(uncoveredSet, complex);
            Rule rule = complex.generateRule(generateRuleName(category, ruleIdx++), category, ontology);
            rules.add(rule);
        }
        return rules;
    }

    private Complex findComplex(Set<Patient> trainingSet, Set<Patient> uncoveredSet, Category category,
                                PremiseProperties premiseProperties) throws PartialStarCreationException {
        LOG.debug("findComplex");
        Star star = new Star();
        Patient positiveSeed = positiveSeed(trainingSet, uncoveredSet, category, premiseProperties);
        Patient negativeSeed = negativeSeed(trainingSet, star, positiveSeed, category, premiseProperties);
        while (positiveSeed != null && negativeSeed != null) {
            Collection<Complex> partialStar = partialStar(positiveSeed, negativeSeed);
            if (partialStar.isEmpty()) {
                LOG.debug("Partial star is empty");
                throw new PartialStarCreationException(positiveSeed, negativeSeed);
            }
            star.intersection(partialStar);
            star.deleteNarrowComplexes();
            sortStar(star, category, trainingSet);
            star.leaveFirstElements(5);
            negativeSeed = negativeSeed(trainingSet, star, positiveSeed, category, premiseProperties);
        }
        return star.get(0);
    }

    private Patient positiveSeed(Set<Patient> trainingSet, Set<Patient> uncoveredSet, Category category,
                                 PremiseProperties premiseProperties) {
        LOG.debug("positiveSeed");
        if (uncoveredSet.isEmpty())
            return null;
        Set<Patient> coveredSet = difference(trainingSet, uncoveredSet);
        Set<Patient> categoryCoveredSet = new HashSet<>();
        for (Patient uncovered : uncoveredSet) {
            if (category.assertPatientInCategory(uncovered)) {
                calculateDistance(uncovered, coveredSet, premiseProperties);
                categoryCoveredSet.add(uncovered);
            }
        }
        return Collections.max(categoryCoveredSet);
    }

    private Patient negativeSeed(Collection<Patient> trainingSet, Star star, Patient positiveSeed, Category category,
                                 PremiseProperties premiseProperties) {
        LOG.debug("negativeSeed");
        List<Patient> negativeSeeds = new ArrayList<>();
        for (Patient patient : trainingSet) {
            if (star.isPatientCovered(patient) && !category.assertPatientInCategory(patient)) {
                negativeSeeds.add(patient);
            }
        }
        if (negativeSeeds.isEmpty())
            return null;
        Set<Patient> positiveSeedSingleton = Collections.singleton(positiveSeed);
        for (Patient negativeSeed : negativeSeeds)
            calculateDistance(negativeSeed, positiveSeedSingleton, premiseProperties);
        return Collections.min(negativeSeeds);
    }

    private void calculateDistance(Patient patient, Collection<Patient> otherPatients, PremiseProperties premiseProperties) {
        LOG.debug("calculateDistance");
        if (otherPatients.isEmpty()) {
            LOG.debug("No other patients. Set patient distance to 0");
            patient.setEvaluation(0);
            return;
        }

        double objectPropertiesEvaluation = premiseProperties.objectProperties
                .stream()
                .mapToDouble(property -> entityPropertyDifferenceEvaluation(patient, otherPatients, property))
                .sum();
        double integerPropertiesEvaluation = premiseProperties.integerProperties
                .stream()
                .mapToDouble(property -> integerPropertyDifferenceEvaluation(patient, otherPatients, property))
                .sum();

        patient.setEvaluation((float) (objectPropertiesEvaluation + integerPropertiesEvaluation));
    }

    private Float entityPropertyDifferenceEvaluation(Patient patient, Collection<Patient> otherPatients, ObjectProperty property) {
        Set<Entity> patientProperties = patient.getEntityProperties(property.getID());
        int difference = otherPatients
                .stream()
                .map(otherPatient -> otherPatient.getEntityProperties(property.getID()))
                .mapToInt(otherPatientProps -> symmetricDifference(otherPatientProps, patientProperties).size())
                .sum();

        return (float) difference / (otherPatients.size() * property.getRangeValues().size());
    }

    private Float integerPropertyDifferenceEvaluation(Patient patient, Collection<Patient> otherPatients, IntegerProperty property) {
        Integer patientProperty = patient.getIntegerProperty(property.getID());
        if (patientProperty == null)
            return 0f;
        int difference = otherPatients
                .stream()
                .map(otherPatient -> otherPatient.getIntegerProperty(property.getID()))
                .mapToInt(otherPatientProp -> abs(patientProperty - otherPatientProp))
                .sum();

        return (float) difference / (otherPatients.size() * (property.getMaxValue() - property.getMinValue()));
    }

    private Collection<Complex> partialStar(Patient positivePatient, Patient negativePatient) {
        Collection<Complex> resultComplexes = new ArrayList<>();
//        resultComplexes.addAll(createComplexes(positivePatient.getSymptoms(), negativePatient.getSymptoms(), Complex::setSymptomSelector));
//        resultComplexes.addAll(createComplexes(positivePatient.getNegativeTests(), negativePatient.getNegativeTests(), Complex::setNegativeTestsSelector));
//        resultComplexes.addAll(createComplexes(positivePatient.getPreviousDiseases(), negativePatient.getPreviousDiseases(), Complex::setPreviousDiseasesSelector));
//
//        Complex ageComplex = createLinearComplex(positivePatient.getAge(), negativePatient.getAge(), Complex::setAgeSelector);
//        Complex heightComplex = createLinearComplex(positivePatient.getHeight(), negativePatient.getHeight(), Complex::setHeightSelector);
//        Complex weightComplex = createLinearComplex(positivePatient.getWeight(), negativePatient.getWeight(), Complex::setWeightSelector);
//
//        if (ageComplex != null) resultComplexes.add(ageComplex);
//        if (heightComplex != null) resultComplexes.add(heightComplex);
//        if (weightComplex != null) resultComplexes.add(weightComplex);

        return resultComplexes;
    }

    private void removeCoveredExamples(Collection<Patient> trainingSet, Complex complex) {
        LOG.debug("removeCoveredExamples");
        trainingSet.removeIf(complex::isPatientCovered);
    }

    private Collection<Complex> createComplexes(Collection<Entity> positiveEntities, Collection<Entity> negativeEntities,
                                                BiConsumer<Complex, EntitiesSelector> complexSetter) {
        ArrayList<Complex> complexes = new ArrayList<>();
        if (!positiveEntities.isEmpty()) {
            for (Entity entity : positiveEntities) {
                if (!negativeEntities.contains(entity)) {
                    Complex complex = createComplex(entity, complexSetter);
                    complexes.add(complex);
                }
            }
        }
        return complexes;
    }

    private Complex createComplex(Entity entity, BiConsumer<Complex, EntitiesSelector> complexSetter) {
        EntitiesSelector selector = new EntitiesSelector();
        selector.add(entity);
        Complex complex = new Complex();
        complexSetter.accept(complex, selector);
        return complex;
    }

    private Complex createLinearComplex(int posVal, int negVal, BiConsumer<Complex, LinearSelector<Integer>> complexSetter) {
        LinearSelector<Integer> selector = createLinearSelector(posVal, negVal);
        if (selector != null) {
            Complex complex = new Complex();
            complexSetter.accept(complex, selector);
            return complex;
        }
        return null;
    }

    private LinearSelector createLinearSelector(int posValue, int negValue) {
        if (posValue >= 0 && negValue >= 0 && posValue != negValue) {
            int midValue = Math.round(posValue + (negValue - posValue) * epsilon);
            if (negValue < posValue) {
                if (midValue == negValue)
                    return LinearSelector.greaterThanSelector(midValue);
                else
                    return LinearSelector.atLeastSelector(midValue);
            } else {
                if (midValue == negValue)
                    return LinearSelector.lessThanSelector(midValue);
                else
                    return LinearSelector.atMostSelector(midValue);
            }
        }
        return null;
    }

    private boolean assertPatientWithCategoryInSet(Set<Patient> patientsSet, Category category) {
        return patientsSet.stream().anyMatch(category::assertPatientInCategory);
    }

    private String generateRuleName(Category category, int ruleIdx) {
        return format("%s_%s_%s_%d", GENERATED_RULE_PREFIX, category.getPredicate(), category.getEntity(), ruleIdx);
    }

    private Collection<Rule> simplifyRules(Collection<Rule> rules) {
        List<Rule> rulesList = new ArrayList<>(rules);
        for (int i = 0; i < rulesList.size() - 1; i++) {
            Rule rule = rulesList.get(i);
            for (int j = i + 1; j < rulesList.size(); j++) {
                Rule referenceRule = rulesList.get(j);
                if (rule.getBodyAtoms().equals(referenceRule.getBodyAtoms())) {
                    rule.addHeadAtoms(referenceRule.getHeadAtoms());
                    rulesList.remove(referenceRule);
                }
            }
        }
        return rulesList;
    }

    private Map<String, Collection<Entity>> mapCategoryClassToEntity(Map<String, Collection<String>> predicateClassCategories) {
        HashMap<String, Collection<Entity>> predicateEntityCategories = new HashMap<>();
        predicateClassCategories.forEach((predicate, classes) -> {
            Set<Entity> entities = classes
                    .stream()
                    .map(ontology::getClassInstances)
                    .flatMap(Collection::stream)
                    .collect(toSet());
            predicateEntityCategories.put(predicate, entities);
        });
        return predicateEntityCategories;
    }

}
