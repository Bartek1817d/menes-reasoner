package pl.edu.agh.plonka.bartlomiej.menes.service;

import org.slf4j.Logger;
import pl.edu.agh.plonka.bartlomiej.menes.exception.PartialStarCreationException;
import pl.edu.agh.plonka.bartlomiej.menes.model.*;
import pl.edu.agh.plonka.bartlomiej.menes.model.rule.*;

import java.util.*;
import java.util.concurrent.*;

import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.symmetricDifference;
import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
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
            Collection<Complex> partialStar = partialStar(positiveSeed, negativeSeed, premiseProperties);
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

    private Collection<Complex> partialStar(Patient positivePatient, Patient negativePatient, PremiseProperties premiseProperties) {
        Collection<Complex> resultComplexes = premiseProperties.objectProperties
                .stream()
                .map(p -> createComplexes(p, positivePatient, negativePatient))
                .flatMap(Collection::stream)
                .collect(toList());

        resultComplexes.addAll(premiseProperties.integerProperties
                .stream()
                .map(p -> createAtomComplex(p, positivePatient, negativePatient))
                .filter(Objects::nonNull)
                .collect(toList()));

        return resultComplexes;
    }

    private void removeCoveredExamples(Collection<Patient> trainingSet, Complex complex) {
        LOG.debug("removeCoveredExamples");
        trainingSet.removeIf(complex::isPatientCovered);
    }

    private Collection<Complex> createComplexes(ObjectProperty property, Patient positivePatient, Patient negativePatient) {
        Set<Entity> positiveProperties = positivePatient.getEntityProperties(property.getID());
        Set<Entity> negativeProperties = negativePatient.getEntityProperties(property.getID());
        return positiveProperties
                .stream()
                .filter(p -> !negativeProperties.contains(p))
                .map(p -> createAtomComplex(property, p))
                .collect(toList());
    }

    private Complex createAtomComplex(ObjectProperty property, Entity entity) {
        EntitiesSelector selector = new EntitiesSelector();
        selector.add(entity);
        Complex complex = new Complex();
        complex.setEntitySelector(property, selector);
        return complex;
    }

    private Complex createAtomComplex(IntegerProperty property, Patient positivePatient, Patient negativePatient) {
        Integer posVal = positivePatient.getIntegerProperty(property.getID());
        Integer negVal = negativePatient.getIntegerProperty(property.getID());
        LinearSelector<Integer> selector = createLinearSelector(posVal, negVal);
        if (selector != null) {
            Complex complex = new Complex();
            complex.setIntegerSelector(property, selector);
            return complex;
        }
        return null;
    }

    private LinearSelector createLinearSelector(Integer posValue, Integer negValue) {
        if (posValue != null && negValue != null && !posValue.equals(negValue)) {
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
}
