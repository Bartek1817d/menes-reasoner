package pl.edu.agh.plonka.bartlomiej.menes.model.rule;

import org.slf4j.Logger;
import pl.edu.agh.plonka.bartlomiej.menes.model.*;
import pl.edu.agh.plonka.bartlomiej.menes.service.OntologyWrapper;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;
import static pl.edu.agh.plonka.bartlomiej.menes.utils.Constants.*;
import static pl.edu.agh.plonka.bartlomiej.menes.utils.Others.findEntity;

public class Complex implements Comparable<Complex> {

    private static final Logger LOG = getLogger(Complex.class);

    private Map<String, LinearSelector<Integer>> linearSelectors = new HashMap<>();
    private Map<String, EntitiesSelector> entitySelectors = new HashMap<>();

    private Float evaluation;

    @SuppressWarnings("unchecked")
    public static Complex conjunction(Complex complex1, Complex complex2) {
        Complex resultComplex = new Complex();

        resultComplex.entitySelectors = mergeMaps(complex1.entitySelectors, complex2.entitySelectors, (s1, s2) -> (EntitiesSelector) setSelector(s1, s2));
        resultComplex.linearSelectors = mergeMaps(complex1.linearSelectors, complex2.linearSelectors, (s1, s2) -> (LinearSelector<Integer>) setSelector(s1, s2));

        return resultComplex;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Selector setSelector(Selector selector1, Selector selector2) {
        if (selector1 != null && selector2 != null)
            return selector1.conjunction(selector2);
        if (selector1 != null)
            return selector1;
        return selector2;
    }

    public static Collection<Complex> intersection(Collection<Complex> complexes1, Collection<Complex> complexes2) {
        List<Complex> resultComplexes = new LinkedList<>();
        for (Complex complex1 : complexes1)
            for (Complex complex2 : complexes2)
                resultComplexes.add(Complex.conjunction(complex1, complex2));
        return resultComplexes;
    }

    private Collection<AbstractAtom> createEntityAtoms(Variable patientVariable) {
        return entitySelectors.entrySet()
                .stream()
                .map(entry -> createEntityAtoms(
                        patientVariable,
                        entry.getKey(),
                        entry.getValue()
                ))
                .flatMap(Collection::stream)
                .collect(toList());
    }

    private static Collection<AbstractAtom> createEntityAtoms(Variable variable, String predicate, Collection<Entity> entities) {
        if (entities == null)
            return Collections.emptyList();
        else
            return entities.stream().map(e -> new TwoArgumentsAtom<>(predicate, variable, e)).collect(toList());
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

    private Collection<AbstractAtom> createLinearAtoms(Variable patientVariable) {
        return linearSelectors.entrySet()
                .stream()
                .map(entry -> createLinearAtoms(
                        new Variable('_' + entry.getKey()),
                        patientVariable,
                        entry.getKey(),
                        entry.getValue()))
                .flatMap(Collection::stream)
                .collect(toList());
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

    public Float getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(Float evaluation) {
        this.evaluation = evaluation;
    }

    public boolean contains(Complex complex) {
        for (Map.Entry<String, EntitiesSelector> entry : entitySelectors.entrySet()) {
            if (!complex.entitySelectors.containsKey(entry.getKey()) || !contains(entry.getValue(), complex.entitySelectors.get(entry.getKey())))
                return false;
        }
        for (Map.Entry<String, LinearSelector<Integer>> entry : linearSelectors.entrySet()) {
            if (!complex.linearSelectors.containsKey(entry.getKey()) || !contains(entry.getValue(), complex.linearSelectors.get(entry.getKey())))
                return false;
        }
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
        for (Map.Entry<String, EntitiesSelector> entry : entitySelectors.entrySet()) {
            if (!covers(entry.getValue(), patient.getEntityProperties(entry.getKey())))
                return false;
        }
        for (Map.Entry<String, LinearSelector<Integer>> entry : linearSelectors.entrySet()) {
            if (!covers(entry.getValue(), patient.getIntegerProperty(entry.getKey())))
                return false;
        }
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
        OntologyClass patientClass = findEntity(PATIENT_CLASS, ontology.getClasses());
        Variable patientVariable = new Variable("patient", patientClass);

        rule.addDeclarationAtom(new ClassDeclarationAtom<>(patientClass, patientVariable));

        rule.addBodyAtoms(createLinearAtoms(patientVariable));
        rule.addBodyAtoms(createEntityAtoms(patientVariable));

        rule.addHeadAtoms(createEntityAtoms(patientVariable, category.getPredicate(), singleton(category.getEntity())));

        return rule;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        BiConsumer<String, Object> format = (property, selector) -> {
            str.append(property);
            str.append(": \n\t");
            str.append(selector);
        };
        entitySelectors.forEach(format);
        linearSelectors.forEach(format);

        return str.toString();
    }

    @Override
    public int compareTo(Complex o) {

        return 0;
    }

    public void setEntitySelector(ObjectProperty property, EntitiesSelector selector) {
        entitySelectors.put(property.getID(), selector);
    }

    public void setIntegerSelector(IntegerProperty property, LinearSelector<Integer> selector) {
        linearSelectors.put(property.getID(), selector);
    }

    private static <T, U> Map<T, U> mergeMaps(Map<T, U> map1, Map<T, U> map2, BinaryOperator<U> conflictResolver) {
        return Stream.of(map1, map2)
                .flatMap(map -> map.entrySet().stream())
                .collect(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        conflictResolver
                ));
    }
}
