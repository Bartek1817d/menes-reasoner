package pl.edu.agh.plonka.bartlomiej.menes.service;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.common.collect.Range;
import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.slf4j.Logger;
import org.swrlapi.core.SWRLAPIOWLOntology;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.core.SWRLRuleRenderer;
import org.swrlapi.factory.SWRLAPIFactory;
import pl.edu.agh.plonka.bartlomiej.menes.exception.CreateRuleException;
import pl.edu.agh.plonka.bartlomiej.menes.model.*;
import pl.edu.agh.plonka.bartlomiej.menes.model.rule.AbstractAtom;
import pl.edu.agh.plonka.bartlomiej.menes.model.rule.Rule;
import pl.edu.agh.plonka.bartlomiej.menes.model.rule.TwoArgumentsAtom;
import pl.edu.agh.plonka.bartlomiej.menes.model.rule.Variable;
import pl.edu.agh.plonka.bartlomiej.menes.utils.NameUtils;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImplPlain;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static java.util.Collections.*;
import static java.util.stream.Collectors.toSet;
import static org.semanticweb.owlapi.search.EntitySearcher.getDataPropertyValues;
import static org.semanticweb.owlapi.search.EntitySearcher.getObjectPropertyValues;
import static org.slf4j.LoggerFactory.getLogger;

public class OntologyWrapper {

    private static final Logger LOG = getLogger(OntologyWrapper.class);

    private final OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();
    private final EntitiesLoader entitiesLoader;
    private final RulesManager rulesManager;
    private final OWLOntology ontology;
    private final OWLDataFactory factory;
    private final PrefixManager prefixManager;
    private final OWLDocumentFormat ontologyFormat;
    private final OWLOntologyManager ontologyManager;
    private final OWLReasoner reasoner;
    private final SWRLRuleEngine ruleEngine;
    private final SWRLAPIOWLOntology ruleOntology;
    private final SWRLRuleRenderer ruleRenderer;
    private Map<String, OntologyClass> classes = new HashMap<>();
    private OWLEntityRemover remover;
    private Map<String, Entity> entities = new HashMap<>();
    private Collection<Rule> rules = new ArrayList<>();
    private OntologyProperties properties;
    private Set<Property> stringProperties;
    private Set<IntegerProperty> integerProperties;
    private Set<ObjectProperty> entityProperties;
    private Set<Patient> patients;

    public OntologyWrapper(String baseURL) throws OWLOntologyCreationException {
        ontologyManager = OWLManager.createOWLOntologyManager();
        factory = ontologyManager.getOWLDataFactory();
        ontology = ontologyManager.createOntology(IRI.create(baseURL));
        ontologyFormat = ontologyManager.getOntologyFormat(ontology);
        prefixManager = ontologyFormat.asPrefixOWLOntologyFormat();
        prefixManager.setDefaultPrefix(baseURL + "#");
        OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
        reasoner = reasonerFactory.createReasoner(ontology, new SimpleConfiguration());
        remover = new OWLEntityRemover(Collections.singleton(ontology));
        ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(ontology);
        ruleOntology = ruleEngine.getSWRLAPIOWLOntology();
        ruleRenderer = ruleOntology.createSWRLRuleRenderer();
        properties = new OntologyProperties(factory, prefixManager);
        entitiesLoader = new EntitiesLoader(ontology, renderer, factory, reasoner, properties);
        rulesManager = new RulesManager(ruleOntology);
    }

    public OntologyWrapper(InputStream inputStream) throws OWLOntologyCreationException {
        ontologyManager = OWLManager.createOWLOntologyManager();
        factory = ontologyManager.getOWLDataFactory();
        ontology = ontologyManager.loadOntologyFromOntologyDocument(inputStream);
        ontologyFormat = ontologyManager.getOntologyFormat(ontology);
        prefixManager = ontologyFormat.asPrefixOWLOntologyFormat();
        String baseURL = ontology.getOntologyID().getOntologyIRI().get().toString();
        prefixManager.setDefaultPrefix(baseURL + "#");
        OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
        reasoner = reasonerFactory.createReasoner(ontology, new SimpleConfiguration());
        remover = new OWLEntityRemover(Collections.singleton(ontology));
        ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(ontology);
        ruleOntology = ruleEngine.getSWRLAPIOWLOntology();
        ruleRenderer = ruleOntology.createSWRLRuleRenderer();
        properties = new OntologyProperties(factory, prefixManager);
        entitiesLoader = new EntitiesLoader(ontology, renderer, factory, reasoner, properties);
        rulesManager = new RulesManager(ruleOntology);
        loadData();
    }

    public OntologyWrapper(File file) throws OWLOntologyCreationException {
        ontologyManager = OWLManager.createOWLOntologyManager();
        factory = ontologyManager.getOWLDataFactory();
        ontology = ontologyManager.loadOntologyFromOntologyDocument(file);
        ontologyFormat = ontologyManager.getOntologyFormat(ontology);
        prefixManager = ontologyFormat.asPrefixOWLOntologyFormat();
        String baseURL = ontology.getOntologyID().getOntologyIRI().get().toString();
        prefixManager.setDefaultPrefix(baseURL + "#");
        OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
        reasoner = reasonerFactory.createReasoner(ontology, new SimpleConfiguration());
        remover = new OWLEntityRemover(Collections.singleton(ontology));
        ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(ontology);
        ruleOntology = ruleEngine.getSWRLAPIOWLOntology();
        ruleRenderer = ruleOntology.createSWRLRuleRenderer();
        properties = new OntologyProperties(factory, prefixManager);
        entitiesLoader = new EntitiesLoader(ontology, renderer, factory, reasoner, properties);
        rulesManager = new RulesManager(ruleOntology);
        loadData();
    }

    private void loadData() {
        classes = entitiesLoader.loadClasses();
        entities = entitiesLoader.loadInstances(classes);
        integerProperties = entitiesLoader.loadIntegerProperties();
        stringProperties = entitiesLoader.loadStringProperties();
        entityProperties = entitiesLoader.loadObjectProperties(classes);
        patients = getPatients();
        fillIntegerPropertiesRanges();
        rules = rulesManager.loadRules(classes, entities);
    }

    private void fillIntegerPropertiesRanges() {
        integerProperties.forEach(this::fillIntegerPropertyRange);
    }

    private void fillIntegerPropertyRange(IntegerProperty property) {
        Set<Integer> propertyValues = patients
                .stream()
                .map(p -> p.getIntegerProperties(property.getID()))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(toSet());
        property.setMinValue(min(propertyValues));
        property.setMaxValue(max(propertyValues));
    }

    public Map<String, OntologyClass> getClasses() {
        return classes;
    }

    public Collection<Entity> getClassInstances(String className) {
        Entity cls = classes.get(className);
        if (cls == null)
            return emptyList();
        return entities.values()
                .stream()
                .filter(e -> e.getClasses().contains(cls))
                .collect(toSet());
    }

    private Patient getPatient(OWLIndividual patientInd) {
        Patient patient = new Patient(renderer.render(patientInd));

        stringProperties.stream().map(Entity::getID).forEach(propertyName ->
                patient.setStringProperties(propertyName, getPatientStringProperties(patientInd, propertyName))
        );
        integerProperties.stream().map(Entity::getID).forEach(propertyName ->
                patient.setIntegerProperties(propertyName, getPatientIntegerProperties(patientInd, propertyName))
        );
        entityProperties.stream().map(Entity::getID).forEach(propertyName ->
                patient.setEntityProperties(propertyName, getPatientObjectProperties(patientInd, propertyName))
        );

        return patient;
    }

    public void addPatient(Patient patient) {
        generatePatientID(patient);
        OWLNamedIndividual patientInd = factory.getOWLNamedIndividual(patient.getID(), prefixManager);

        setIndClass(properties.patientClass, patientInd);

        stringProperties.stream().map(Entity::getID).forEach(propertyName ->
                setPatientIndStringProperty(patientInd, propertyName, patient.getStringProperties(propertyName))
        );
        integerProperties.stream().map(Entity::getID).forEach(propertyName ->
                setPatientIndIntegerProperty(patientInd, propertyName, patient.getIntegerProperties(propertyName))
        );
        entityProperties.stream().map(Entity::getID).forEach(propertyName ->
                setPatientIndObjectProperty(patientInd, propertyName, patient.getEntityProperties(propertyName))
        );

//        getInferredPatient(patient);
    }

    public Patient updatePatient(Patient patient) {
        deleteEntity(patient);
        addPatient(patient);
//        return getInferredPatient(patient);
        return patient;
    }

    public Set<Patient> getPatients() {
        Set<Patient> patients = new HashSet<>();
        Patient patient;
        for (OWLIndividual patientInd : EntitySearcher.getIndividuals(properties.patientClass, ontology)) {
            patient = getPatient(patientInd);
//            patient = getInferredPatient(patient);
            patients.add(patient);
        }
//        patients.addAll(generatePatientsFromRules());
        return patients;
    }

    public void addEntity(Entity entity) {
        OWLNamedIndividual entityInd = factory.getOWLNamedIndividual(entity.getID(), prefixManager);
        setEntityIndClasses(entityInd, entity.getClasses());
        setEntityIndProperty(entityInd, factory.getRDFSLabel(), entity.getLanguageLabelMap());
        setEntityIndProperty(entityInd, factory.getRDFSComment(), entity.getLanguageCommentMap());
    }

    public void deleteEntity(Entity entity) {
        OWLNamedIndividual entityID = factory.getOWLNamedIndividual(entity.getID(), prefixManager);
        entityID.accept(remover);
        ontologyManager.applyChanges(remover.getChanges());
        remover.reset();
    }

    public void deleteEntities(Collection<Entity> entities) {
        for (Entity entity : entities)
            deleteEntity(entity);
    }

    public void deletePatients(Collection<Patient> patients) {
        for (Entity patient : patients)
            deleteEntity(patient);
    }

    public void saveOntologyToFile(File file) throws OWLOntologyStorageException {
        ontologyManager.saveOntology(ontology, ontologyFormat, IRI.create(file));
    }

    public boolean containsID(String id) {
        return ontology.containsEntityInSignature(IRI.create(prefixManager.getDefaultPrefix(), id));
    }

    public Collection<Rule> getRules() {
        return rules;
    }

    public void addRule(Rule rule) throws CreateRuleException {
        rulesManager.addRule(rule);
    }

    public void deleteRule(Rule rule) {
        rulesManager.deleteRule(rule);
    }

    public void deleteRules(Collection<Rule> rules) {
        rulesManager.deleteRules(rules);
    }

    public void changeLanguage() {
        classes.values().forEach(Entity::setLanguage);
        entities.values().forEach(Entity::setLanguage);
    }

    public Patient getInferredPatient(Patient patient) {
        reasoner.flush();
        OWLNamedIndividual patientInd = factory.getOWLNamedIndividual(patient.getID(), prefixManager);

        integerProperties.stream().map(Entity::getID).forEach(propertyName ->
                patient.setInferredIntegerProperties(
                        propertyName,
                        getPatientInferredIntegerProperty(patientInd, propertyName, patient.getIntegerProperties(propertyName))));
        stringProperties.stream().map(Entity::getID).forEach(propertyName ->
                patient.setInferredStringProperties(
                        propertyName,
                        getPatientInferredStringProperty(patientInd, propertyName, patient.getStringProperties(propertyName))));
        entityProperties.stream().map(Entity::getID).forEach(propertyName ->
                patient.setInferredEntityProperties(
                        propertyName,
                        getPatientInferredObjectProperty(patientInd, propertyName, patient.getEntityProperties(propertyName))));

        return patient;
    }

    private void addEntityToPatient(TwoArgumentsAtom atom, Map<String, Entity> variables, Method patientMethod) {
        String pName = ((Variable) atom.getArgument1()).getName();
        Patient p = (Patient) variables.get(pName);
        try {
            patientMethod.invoke(p, (Entity) atom.getArgument2());
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void setPatientAge(Rule rule, TwoArgumentsAtom atom, Map<String, Entity> variables) {
        Variable patientVariable = (Variable) atom.getArgument1();
        Variable ageVariable = (Variable) atom.getArgument2();
        Patient p = (Patient) variables.get(patientVariable.getName());
        Range<Integer> ageRange = calculateAgeRange(rule, ageVariable);

//        p.setAge(selectNumberFromRange(ageRange));
    }

    private Range<Integer> calculateAgeRange(Rule rule, Variable ageVariable) {
        Range<Integer> ageRange = Range.all();
        for (AbstractAtom atom : rule.getBodyAtoms()) {
            if (atom instanceof TwoArgumentsAtom) {
                TwoArgumentsAtom twoArgumentsAtom = (TwoArgumentsAtom) atom;
                if (atom.getPrefix().equals("swrlb")
                        && twoArgumentsAtom.getArgument1().equals(ageVariable)
                        && twoArgumentsAtom.getArgument2() instanceof Integer) {
                    int ageBound = (int) twoArgumentsAtom.getArgument2();
                    ageRange = intersection(ageRange, atom.getPredicate(), ageBound);
                }
            }
        }
        return ageRange;
    }

    private Range<Integer> intersection(Range<Integer> range, String operator, Integer bound) {
        switch (operator) {
            case "equal":
                return range.intersection(Range.singleton(bound));
            case "greaterThan":
                return range.intersection(Range.greaterThan(bound));
            case "greaterThanOrEqual":
                return range.intersection(Range.atLeast(bound));
            case "lessThan":
                return range.intersection(Range.lessThan(bound));
            case "lessThanOrEqual":
                return range.intersection(Range.atMost(bound));
            default:
                return range;
        }
    }

    private Collection<String> getPatientStringProperties(OWLIndividual patientInd, String propertyName) {
        OWLDataProperty property = factory.getOWLDataProperty(propertyName, prefixManager);
        return getDataPropertyValues(patientInd, property, ontology)
                .stream()
                .map(renderer::render)
                .collect(toSet());
    }

    private Collection<Integer> getPatientIntegerProperties(OWLIndividual patientInd, String propertyName) {
        OWLDataProperty property = factory.getOWLDataProperty(propertyName, prefixManager);
        return getDataPropertyValues(patientInd, property, ontology)
                .stream()
                .map(renderer::render)
                .map(Integer::parseInt)
                .collect(toSet());
    }

    private Collection<Entity> getPatientObjectProperties(OWLIndividual patientInd, String propertyName) {
        OWLObjectProperty property = factory.getOWLObjectProperty(propertyName, prefixManager);
        return getObjectPropertyValues(patientInd, property, ontology)
                .stream()
                .map(v -> entities.get(renderer.render(v)))
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    private Collection<Integer> getPatientInferredIntegerProperty(OWLNamedIndividual patientInd, String propertyName, Collection<Integer> assertedValues) {
        OWLDataProperty property = factory.getOWLDataProperty(propertyName, prefixManager);
        Set<Integer> inferredEntities = reasoner.getDataPropertyValues(patientInd, property)
                .stream()
                .map(renderer::render)
                .map(Integer::parseInt)
                .collect(toSet());
        inferredEntities.removeAll(assertedValues);
        return inferredEntities;
    }

    private Collection<String> getPatientInferredStringProperty(OWLNamedIndividual patientInd, String propertyName, Collection<String> assertedValues) {
        OWLDataProperty property = factory.getOWLDataProperty(propertyName, prefixManager);
        Set<String> inferredEntities = reasoner.getDataPropertyValues(patientInd, property)
                .stream()
                .map(renderer::render)
                .collect(toSet());
        inferredEntities.removeAll(assertedValues);
        return inferredEntities;
    }

    private Collection<Entity> getPatientInferredObjectProperty(OWLNamedIndividual patientInd, String propertyName, Collection<Entity> assertedValues) {
        OWLObjectProperty property = factory.getOWLObjectProperty(propertyName, prefixManager);
        Set<Entity> inferredEntities = reasoner.getObjectPropertyValues(patientInd, property).getFlattened()
                .stream()
                .map(v -> entities.get(renderer.render(v)))
                .collect(toSet());
        inferredEntities.removeAll(assertedValues);
        return inferredEntities;
    }

    private void setPatientIndStringProperty(OWLIndividual patientInd, String propertyName, Collection<String> values) {
        OWLDataProperty property = factory.getOWLDataProperty(propertyName, prefixManager);
        values
                .stream()
                .filter(StringUtils::isNotBlank)
                .forEach(v -> ontologyManager.addAxiom(
                        ontology,
                        factory.getOWLDataPropertyAssertionAxiom(property, patientInd, v)));
    }

    private void setPatientIndIntegerProperty(OWLIndividual patientInd, String propertyName, Collection<Integer> values) {
        OWLDataProperty property = factory.getOWLDataProperty(propertyName, prefixManager);
        values
                .stream()
                .filter(Objects::nonNull)
                .forEach(v -> ontologyManager.addAxiom(
                        ontology,
                        factory.getOWLDataPropertyAssertionAxiom(property, patientInd, v)));
    }

    private void setPatientIndObjectProperty(OWLIndividual patientInd, String propertyName, Collection<Entity> entities) {
        OWLObjectProperty property = factory.getOWLObjectProperty(propertyName, prefixManager);
        entities
                .stream()
                .filter(Objects::nonNull)
                .forEach(v -> ontologyManager.addAxiom(
                        ontology,
                        factory.getOWLObjectPropertyAssertionAxiom(
                                property,
                                patientInd,
                                factory.getOWLNamedIndividual(v.getID(), prefixManager))));
    }

    private void setEntityIndClasses(OWLNamedIndividual entityInd, Collection<Entity> classes) {
        for (Entity cls : classes) {
            OWLClass owlClass = factory.getOWLClass(cls.getID(), prefixManager);
            setIndClass(owlClass, entityInd);
        }
    }

    private void setEntityIndProperty(OWLNamedIndividual entityInd, OWLAnnotationProperty property, Map<String, String> languageMap) {
        if (languageMap == null) {
            LOG.warn("Language map is null. Cannot save {}.", entityInd);
            return;
        }
        languageMap.forEach((language, value) -> {
            ontologyManager.addAxiom(ontology, factory.getOWLAnnotationAssertionAxiom(property,
                    entityInd.getIRI(), new OWLLiteralImplPlain(value, language)));
        });
    }

    private void setIndClass(OWLClassExpression classExpression, OWLIndividual individual) {
        ontologyManager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(classExpression, individual));
    }

    private void generatePatientID(Patient patient) {
        if (patient.getID() == null) {
            String fn = patient.getFirstName();
            String ln = patient.getLastName();
            if (containsID(NameUtils.generateName(fn, ln))) {
                int i = 1;
                String newID = NameUtils.generateName(fn, ln, Integer.toString(i));
                while (containsID(newID)) {
                    newID = NameUtils.generateName(fn, ln, Integer.toString(++i));
                }
                patient.setID(newID);
            } else
                patient.setID(NameUtils.generateName(fn, ln));
        }
    }
}
