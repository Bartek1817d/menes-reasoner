package pl.edu.agh.plonka.bartlomiej.menes.model.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.plonka.bartlomiej.menes.model.Entity;
import pl.edu.agh.plonka.bartlomiej.menes.model.Patient;
import pl.edu.agh.plonka.bartlomiej.menes.service.OntologyWrapper;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;
import static pl.edu.agh.plonka.bartlomiej.menes.utils.Constants.*;

public class Complex implements Comparable<Complex> {

    private static final Logger LOG = LoggerFactory.getLogger(Complex.class);

    private EntitiesSelector<Entity> previousDiseasesSelector;
    private EntitiesSelector<Entity> symptomSelector;
    private EntitiesSelector<Entity> negativeTestsSelector;
    private LinearSelector<Integer> ageSelector;
    private LinearSelector<Integer> heightSelector;
    private LinearSelector<Integer> weightSelector;

    private Float evaluation;

    @SuppressWarnings("unchecked")
    public static Complex conjunction(Complex complex1, Complex complex2) {
        Complex resultComplex = new Complex();
        resultComplex.previousDiseasesSelector = (EntitiesSelector<Entity>) setSelector(
                complex1.previousDiseasesSelector, complex2.previousDiseasesSelector);
        resultComplex.symptomSelector = (EntitiesSelector<Entity>) setSelector(complex1.symptomSelector,
                complex2.symptomSelector);
        resultComplex.negativeTestsSelector = (EntitiesSelector<Entity>) setSelector(complex1.negativeTestsSelector,
                complex2.negativeTestsSelector);
        resultComplex.ageSelector = (LinearSelector<Integer>) setSelector(complex1.ageSelector, complex2.ageSelector);
        resultComplex.heightSelector = (LinearSelector<Integer>) setSelector(complex1.heightSelector, complex2.heightSelector);
        resultComplex.weightSelector = (LinearSelector<Integer>) setSelector(complex1.weightSelector, complex2.weightSelector);

        return resultComplex;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Selector setSelector(Selector selector1, Selector selector2) {
        if (selector1 != null && selector2 != null)
            return selector1.conjunction(selector2);
        if (selector1 != null)
            return selector1;
        if (selector2 != null)
            return selector2;
        return null;
    }

    public static Collection<Complex> intersection(Collection<Complex> complexes1, Collection<Complex> complexes2) {
        List<Complex> resultComplexes = new LinkedList<>();
        for (Complex complex1 : complexes1)
            for (Complex complex2 : complexes2)
                resultComplexes.add(Complex.conjunction(complex1, complex2));
        return resultComplexes;
    }

    private static Collection<AbstractAtom> createEntityAtoms(Variable variable, String predicate, Collection<Entity> entities) {
        if (entities == null)
            return Collections.emptyList();
        else
            return entities.stream().map(e -> new TwoArgumentsAtom<>(predicate, variable, e)).collect(Collectors.toList());
    }

    private static Collection<AbstractAtom> createLinearAtoms(Variable linearVariable, Variable patientVariable, String propertyName,
                                                              LinearSelector<Integer> linearSelector) {
        if (linearSelector == null)
            return Collections.emptyList();

        Collection<AbstractAtom> atoms = new ArrayList<>();
        atoms.add(new TwoArgumentsAtom<>(propertyName, patientVariable, linearVariable));
        atoms.addAll(createLinearAtoms(linearVariable, linearSelector));

        return atoms;
    }

    private static Collection<AbstractAtom> createLinearAtoms(Variable linearVariable, LinearSelector<Integer> linearSelector) {
        ArrayList<AbstractAtom> atoms = new ArrayList<>();
        if (linearSelector.hasLowerBound() && linearSelector.hasUpperBound()
                && linearSelector.lowerEndpoint().equals(linearSelector.upperEndpoint())) {
            TwoArgumentsAtom<Variable, Integer> equalAtom = new TwoArgumentsAtom<>(
                    EQUAL_PROPERTY, SWRLB_PREFIX, linearVariable, linearSelector.lowerEndpoint());
            atoms.add(equalAtom);
        } else {
            if (linearSelector.hasLowerBound()) {
                switch (linearSelector.lowerBoundType()) {
                    case OPEN:
                        TwoArgumentsAtom<Variable, Integer> greaterThanAtom = new TwoArgumentsAtom<>(
                                GREATER_THAN_PROPERTY, SWRLB_PREFIX, linearVariable, linearSelector.lowerEndpoint());
                        atoms.add(greaterThanAtom);
                        break;
                    case CLOSED:
                        TwoArgumentsAtom<Variable, Integer> atLeastAtom = new TwoArgumentsAtom<>(
                                GREATER_THAN_OR_EQUAL_PROPERTY, SWRLB_PREFIX, linearVariable, linearSelector.lowerEndpoint());
                        atoms.add(atLeastAtom);
                        break;
                }
            }
            if (linearSelector.hasUpperBound()) {
                switch (linearSelector.upperBoundType()) {
                    case OPEN:
                        TwoArgumentsAtom<Variable, Integer> lessThanAtom = new TwoArgumentsAtom<>(
                                LESS_THAN_PROPERTY, SWRLB_PREFIX, linearVariable, linearSelector.upperEndpoint());
                        atoms.add(lessThanAtom);
                        break;
                    case CLOSED:
                        TwoArgumentsAtom<Variable, Integer> atMostAtom = new TwoArgumentsAtom<>(
                                LESS_THAN_OR_EQUAL_PROPERTY, SWRLB_PREFIX, linearVariable, linearSelector.upperEndpoint());
                        atoms.add(atMostAtom);
                        break;
                }
            }
        }
        return atoms;
    }

    public EntitiesSelector<Entity> getPreviousDiseasesSelector() {
        return previousDiseasesSelector;
    }

    public void setPreviousDiseasesSelector(EntitiesSelector<Entity> previousDiseasesSelector) {
        this.previousDiseasesSelector = previousDiseasesSelector;
    }

    public EntitiesSelector<Entity> getSymptomSelector() {
        return symptomSelector;
    }

    public void setSymptomSelector(EntitiesSelector<Entity> symptomSelector) {
        this.symptomSelector = symptomSelector;
    }

    public LinearSelector<Integer> getAgeSelector() {
        return ageSelector;
    }

    public void setAgeSelector(LinearSelector<Integer> ageSelector) {
        this.ageSelector = ageSelector;
    }

    public EntitiesSelector<Entity> getNegativeTestsSelector() {
        return negativeTestsSelector;
    }

    public void setNegativeTestsSelector(EntitiesSelector<Entity> negativeTestsSelector) {
        this.negativeTestsSelector = negativeTestsSelector;
    }

    public LinearSelector<Integer> getHeightSelector() {
        return heightSelector;
    }

    public void setHeightSelector(LinearSelector<Integer> heightSelector) {
        this.heightSelector = heightSelector;
    }

    public LinearSelector<Integer> getWeightSelector() {
        return weightSelector;
    }

    public void setWeightSelector(LinearSelector<Integer> weightSelector) {
        this.weightSelector = weightSelector;
    }

    public Float getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(Float evaluation) {
        this.evaluation = evaluation;
    }

    public boolean contains(Complex complex) {
        if (!contains(symptomSelector, complex.symptomSelector))
            return false;
        if (!contains(negativeTestsSelector, complex.negativeTestsSelector))
            return false;
        if (!contains(previousDiseasesSelector, complex.previousDiseasesSelector))
            return false;
        if (!contains(ageSelector, complex.ageSelector))
            return false;
        if (!contains(heightSelector, complex.heightSelector))
            return false;
        if (!contains(weightSelector, complex.weightSelector))
            return false;
        return true;
    }

    private boolean contains(Selector selector1, Selector selector2) {
        if (selector1 != null) {
            return selector1.contains(selector2);
        } else {
            return true;
        }
    }

    public boolean isPatientCovered(Patient patient) {
        //TODO
//        if (!covers(symptomSelector, patient.getSymptoms())) {
//            return false;
//        }
//        if (!covers(negativeTestsSelector, patient.getNegativeTests())) {
//            return false;
//        }
//        if (!covers(previousDiseasesSelector, patient.getPreviousDiseases())) {
//            return false;
//        }
//        if (!covers(ageSelector, patient.getAge())) {
//            return false;
//        }
//        if (!covers(heightSelector, patient.getHeight())) {
//            return false;
//        }
//        if (!covers(weightSelector, patient.getWeight())) {
//            return false;
//        }
        return true;
    }

    private boolean covers(Selector<Entity> selector, Collection<Entity> entities) {
        return selector == null || selector.covers(entities);
    }

    private boolean covers(Selector<Integer> selector, Integer entity) {
        return selector == null || selector.covers(entity);
    }

    public Rule generateRule(String ruleName, Category category, OntologyWrapper ontology) {
        Rule rule = new Rule(ruleName);
        Variable patientVariable = new Variable("patient", ontology.getClasses().get(PATIENT_CLASS));
        Variable ageVariable = new Variable("_age");
        Variable heightVariable = new Variable("_height");
        Variable weightVariable = new Variable("_weight");

        rule.addDeclarationAtom(new ClassDeclarationAtom<>(ontology.getClasses().get(PATIENT_CLASS), patientVariable));

        rule.addBodyAtoms(createLinearAtoms(ageVariable, patientVariable, AGE_PROPERTY, ageSelector));
        rule.addBodyAtoms(createLinearAtoms(heightVariable, patientVariable, HEIGHT_PROPERTY, heightSelector));
        rule.addBodyAtoms(createLinearAtoms(weightVariable, patientVariable, WEIGHT_PROPERTY, weightSelector));
        rule.addBodyAtoms(createEntityAtoms(patientVariable, HAS_SYMPTOM_PROPERTY, symptomSelector));
        rule.addBodyAtoms(createEntityAtoms(patientVariable, PREVIOUS_DISEASE_PROPERTY, previousDiseasesSelector));
        rule.addBodyAtoms(createEntityAtoms(patientVariable, NEGATIVE_TEST_PROPERTY, negativeTestsSelector));

        rule.addHeadAtoms(createEntityAtoms(patientVariable, category.getPredicate(), singleton(category.getEntity())));

        return rule;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Symptoms: \n\t");
        str.append(symptomSelector);
        str.append("\nPrevious or current diseases: \n\t");
        str.append(previousDiseasesSelector);
        str.append("\nNegative tests: \n\t");
        str.append(negativeTestsSelector);
        str.append("\nAge: ");
        str.append(ageSelector);
        str.append("\nHeight: ");
        str.append(heightSelector);
        str.append("\nWeight: ");
        str.append(weightSelector);
        return str.toString();
    }

    @Override
    public int compareTo(Complex o) {

        return 0;
    }

}
