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
import pl.edu.agh.plonka.bartlomiej.menes.model.Properties;
import pl.edu.agh.plonka.bartlomiej.menes.model.*;
import pl.edu.agh.plonka.bartlomiej.menes.model.rule.AbstractAtom;
import pl.edu.agh.plonka.bartlomiej.menes.model.rule.Rule;
import pl.edu.agh.plonka.bartlomiej.menes.model.rule.TwoArgumentsAtom;
import pl.edu.agh.plonka.bartlomiej.menes.model.rule.Variable;
import pl.edu.agh.plonka.bartlomiej.menes.utils.NameUtils;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImplPlain;

import java.io.File;
import java.io.InputStream;
import java.util.*;

import static java.util.Collections.*;
import static java.util.stream.Collectors.toSet;
import static org.semanticweb.owlapi.search.EntitySearcher.getDataPropertyValues;
import static org.semanticweb.owlapi.search.EntitySearcher.getObjectPropertyValues;
import static org.slf4j.LoggerFactory.getLogger;
import static pl.edu.agh.plonka.bartlomiej.menes.utils.Others.findEntity;

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
    private Set<OntologyClass> classes = new HashSet<>();
    private final OWLEntityRemover remover;
    private Set<Entity> entities = new HashSet<>();
    private Collection<Rule> rules = new ArrayList<>();
    private final OntologyProperties properties;
    private Set<Property> stringProperties;
    private Set<Property> booleanProperties;
    private Set<NumericProperty> numericProperties;
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
        Properties properties = entitiesLoader.loadProperties(classes);
        numericProperties = properties.numericProperties;
        booleanProperties = properties.booleanProperties;
        stringProperties = properties.stringProperties;
        entityProperties = properties.entityProperties;
        patients = getPatients();
        fillIntegerPropertiesRanges();
        rules = rulesManager.loadRules(classes, entities);
    }

    private void fillIntegerPropertiesRanges() {
        numericProperties.forEach(this::fillIntegerPropertyRange);
    }

    private void fillIntegerPropertyRange(NumericProperty property) {
        Set<Float> propertyValues = patients
                .stream()
                .map(p -> p.getNumericProperties(property.getID()))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(toSet());
        property.setMinValue(min(propertyValues));
        property.setMaxValue(max(propertyValues));
    }

    public Set<OntologyClass> getClasses() {
        return classes;
    }

    public Set<Entity> getClassInstances(String className) {
        OntologyClass cls = findEntity(className, classes);
        if (cls == null)
            return emptySet();
        return cls.getInstances();
    }

    private Patient getPatient(OWLIndividual patientInd) {
        Patient patient = new Patient(renderer.render(patientInd));

        stringProperties.stream().map(Entity::getID).forEach(propertyName ->
                patient.setStringProperties(propertyName, getPatientStringProperties(patientInd, propertyName))
        );
        numericProperties.stream().map(Entity::getID).forEach(propertyName ->
                patient.setNumericProperties(propertyName, getPatientNumericProperties(patientInd, propertyName))
        );
        entityProperties.stream().map(Entity::getID).forEach(propertyName ->
                patient.setEntityProperties(propertyName, getPatientObjectProperties(patientInd, propertyName))
        );
        booleanProperties.stream().map(Entity::getID).forEach(propertyName ->
                patient.setBooleanProperties(propertyName, getPatientBooleanProperties(patientInd, propertyName))
        );

        return patient;
    }

    private Collection<Boolean> getPatientBooleanProperties(OWLIndividual patientInd, String propertyName) {
        OWLDataProperty property = factory.getOWLDataProperty(propertyName, prefixManager);
        return getDataPropertyValues(patientInd, property, ontology)
                .stream()
                .map(renderer::render)
                .map(Boolean::parseBoolean)
                .collect(toSet());

    }

    public void addPatient(Patient patient) {
        generatePatientID(patient);
        OWLNamedIndividual patientInd = factory.getOWLNamedIndividual(patient.getID(), prefixManager);

        setIndClass(properties.patientClass, patientInd);

        stringProperties.stream().map(Entity::getID).forEach(propertyName ->
                setPatientIndStringProperty(patientInd, propertyName, patient.getStringProperties(propertyName))
        );
        numericProperties.stream().map(Entity::getID).forEach(propertyName ->
                setPatientIndNumericProperty(patientInd, propertyName, patient.getNumericProperties(propertyName))
        );
        entityProperties.stream().map(Entity::getID).forEach(propertyName ->
                setPatientIndObjectProperty(patientInd, propertyName, patient.getEntityProperties(propertyName))
        );
        booleanProperties.stream().map(Entity::getID).forEach(propertyName ->
                setPatientIndBooleanProperty(patientInd, propertyName, patient.getBooleanProperties(propertyName))
        );

//        getInferredPatient(patient);
    }

    private void setPatientIndBooleanProperty(OWLIndividual patientInd, String propertyName, Collection<Boolean> values) {
        OWLDataProperty property = factory.getOWLDataProperty(propertyName, prefixManager);
        values
                .stream()
                .filter(Objects::nonNull)
                .forEach(v -> ontologyManager.addAxiom(
                        ontology,
                        factory.getOWLDataPropertyAssertionAxiom(property, patientInd, v)));
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

    public void deleteRules() {
        rulesManager.deleteRules();
    }

    public void addRules(Collection<Rule> rules) throws CreateRuleException {
        for (Rule rule : rules) {
            rulesManager.addRule(rule);
        }
    }

    public void changeLanguage() {
        classes.forEach(Entity::setLanguage);
        entities.forEach(Entity::setLanguage);
    }

    public Patient getInferredPatient(Patient patient) {
        reasoner.flush();
        OWLNamedIndividual patientInd = factory.getOWLNamedIndividual(patient.getID(), prefixManager);

        numericProperties.stream().map(Entity::getID).forEach(propertyName ->
                patient.setInferredNumericProperties(
                        propertyName,
                        getPatientInferredNumericProperty(patientInd, propertyName, patient.getNumericProperties(propertyName))));
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

    private Collection<Float> getPatientNumericProperties(OWLIndividual patientInd, String propertyName) {
        OWLDataProperty property = factory.getOWLDataProperty(propertyName, prefixManager);
        return getDataPropertyValues(patientInd, property, ontology)
                .stream()
                .map(renderer::render)
                .map(Float::parseFloat)
                .collect(toSet());
    }

    private Collection<Entity> getPatientObjectProperties(OWLIndividual patientInd, String propertyName) {
        OWLObjectProperty property = factory.getOWLObjectProperty(propertyName, prefixManager);
        return getObjectPropertyValues(patientInd, property, ontology)
                .stream()
                .map(v -> findEntity(renderer.render(v), entities))
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    private Collection<Float> getPatientInferredNumericProperty(OWLNamedIndividual patientInd, String propertyName, Collection<Float> assertedValues) {
        OWLDataProperty property = factory.getOWLDataProperty(propertyName, prefixManager);
        Set<Float> inferredEntities = reasoner.getDataPropertyValues(patientInd, property)
                .stream()
                .map(renderer::render)
                .map(Float::parseFloat)
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
                .map(v -> findEntity(renderer.render(v), entities))
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

    private void setPatientIndNumericProperty(OWLIndividual patientInd, String propertyName, Collection<Float> values) {
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

    private void setEntityIndClasses(OWLNamedIndividual entityInd, Set<OntologyClass> classes) {
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

    public Set<Property> getStringProperties() {
        return stringProperties;
    }

    public Set<NumericProperty> getNumericProperties() {
        return numericProperties;
    }

    public Set<ObjectProperty> getEntityProperties() {
        return entityProperties;
    }
}
