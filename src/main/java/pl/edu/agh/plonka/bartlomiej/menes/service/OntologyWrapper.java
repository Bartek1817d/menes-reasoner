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
import pl.edu.agh.plonka.bartlomiej.menes.model.Entity;
import pl.edu.agh.plonka.bartlomiej.menes.model.Patient;
import pl.edu.agh.plonka.bartlomiej.menes.model.rule.*;
import pl.edu.agh.plonka.bartlomiej.menes.utils.NameUtils;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImplPlain;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.BoundType.OPEN;
import static java.util.stream.Collectors.toSet;
import static org.semanticweb.owlapi.search.EntitySearcher.getDataPropertyValues;
import static org.semanticweb.owlapi.search.EntitySearcher.getObjectPropertyValues;
import static org.slf4j.LoggerFactory.getLogger;

public class OntologyWrapper {

    private static final Logger LOG = getLogger(OntologyWrapper.class);

    private static final Integer MIN_AGE = 0;
    private static final Integer MAX_AGE = 100;
    private static final Pattern diseasePattern = Pattern.compile("(?<diseaseID>\\w+)Disease(?<number>\\d+)");
    private static final Random random = new Random();
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
    private Map<String, Entity> classes = new HashMap<>();
    private OWLEntityRemover remover;
    private Map<String, Entity> entities = new HashMap<>();
    //TODO remove
    private Map<String, Entity> symptoms = new HashMap<>();
    private Map<String, Entity> diseases = new HashMap<>();
    private Map<String, Entity> tests = new HashMap<>();
    private Map<String, Entity> treatments = new HashMap<>();
    private Map<String, Entity> causes = new HashMap<>();
    private Collection<Rule> rules = new ArrayList<>();
    private OntologyProperties properties;
    private Set<String> stringProperties;
    private Set<String> integerProperties;
    private Set<String> entityProperties;

    public OntologyWrapper(String baseURL, Set<String> integerProperties, Set<String> stringProperties, Set<String> entityProperties) throws OWLOntologyCreationException {
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
        entitiesLoader = new EntitiesLoader(ontology, renderer, factory, reasoner);
        rulesManager = new RulesManager(ruleOntology);
        this.integerProperties = integerProperties;
        this.stringProperties = stringProperties;
        this.entityProperties = entityProperties;
    }

    public OntologyWrapper(InputStream inputStream, Set<String> integerProperties, Set<String> stringProperties, Set<String> entityProperties) throws OWLOntologyCreationException {
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
        entitiesLoader = new EntitiesLoader(ontology, renderer, factory, reasoner);
        rulesManager = new RulesManager(ruleOntology);
        this.integerProperties = integerProperties;
        this.stringProperties = stringProperties;
        this.entityProperties = entityProperties;
        loadData();
    }

    public OntologyWrapper(File file, Set<String> integerProperties, Set<String> stringProperties, Set<String> entityProperties) throws OWLOntologyCreationException {
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
        entitiesLoader = new EntitiesLoader(ontology, renderer, factory, reasoner);
        rulesManager = new RulesManager(ruleOntology);
        this.integerProperties = integerProperties;
        this.stringProperties = stringProperties;
        this.entityProperties = entityProperties;
        loadData();
    }

    private void loadData() {
        classes = entitiesLoader.loadClasses();
        symptoms = entitiesLoader.loadInstances(properties.symptomClass, classes);
        diseases = entitiesLoader.loadInstances(properties.diseaseClass, classes);
        tests = entitiesLoader.loadInstances(properties.testingClass, classes);
        treatments = entitiesLoader.loadInstances(properties.treatmentClass, classes);
        causes = entitiesLoader.loadInstances(properties.causeClass, classes);
        rules = rulesManager.loadRules(classes, symptoms, diseases, tests, treatments, causes);
    }

    public Map<String, Entity> getClasses() {
        return classes;
    }

    public Map<String, Entity> getSymptoms() {
        return symptoms;
    }

    public Map<String, Entity> getDiseases() {
        return diseases;
    }

    public Map<String, Entity> getTests() {
        return tests;
    }

    public Map<String, Entity> getTreatments() {
        return treatments;
    }

    public Map<String, Entity> getCauses() {
        return causes;
    }

    private Patient getPatient(OWLIndividual patientInd) {
        Patient patient = new Patient(renderer.render(patientInd));

        stringProperties.forEach(propertyName ->
                patient.setStringProperties(propertyName, getPatientStringProperties(patientInd, propertyName))
        );
        integerProperties.forEach(propertyName ->
                patient.setIntegerProperties(propertyName, getPatientIntegerProperties(patientInd, propertyName))
        );
        entityProperties.forEach(propertyName ->
                patient.setEntityProperties(propertyName, getPatientObjectProperties(patientInd, propertyName, entities))
        );

        return patient;
    }

    public void addPatient(Patient patient) {
        generatePatientID(patient);
        OWLNamedIndividual patientInd = factory.getOWLNamedIndividual(patient.getID(), prefixManager);

        setIndClass(properties.patientClass, patientInd);

        stringProperties.forEach(propertyName ->
                setPatientIndStringProperty(patientInd, propertyName, patient.getStringProperties(propertyName))
        );
        integerProperties.forEach(propertyName ->
                setPatientIndIntegerProperty(patientInd, propertyName, patient.getIntegerProperties(propertyName))
        );
        entityProperties.forEach(propertyName ->
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

    public List<Patient> getPatients() {
        List<Patient> patients = new ArrayList<>();
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Collection<Patient> generatePatientsFromRules() {
        Collection<Patient> patients = new ArrayList<>();
        for (Rule rule : rules) {
            Patient patient = generatePatientFromRule(rule);
            if (patient != null)
                patients.add(patient);
        }
        return patients;
    }

    public void changeLanguage() {
        classes.values().forEach(Entity::setLanguage);
        symptoms.values().forEach(Entity::setLanguage);
        diseases.values().forEach(Entity::setLanguage);
        tests.values().forEach(Entity::setLanguage);
        treatments.values().forEach(Entity::setLanguage);
        causes.values().forEach(Entity::setLanguage);
    }

    public Patient getInferredPatient(Patient patient) {
        reasoner.flush();
        OWLNamedIndividual patientInd = factory.getOWLNamedIndividual(patient.getID(), prefixManager);

        //TODO
/*
        setPatientInferredObjectProperty(patientInd, properties.symptomProperty, symptoms, patient::getSymptoms, patient::setInferredSymptoms);
        setPatientInferredObjectProperty(patientInd, properties.diseaseProperty, diseases, patient::getDiseases, patient::setInferredDiseases);
        setPatientInferredObjectProperty(patientInd, properties.testProperty, tests, patient::getTests, patient::setInferredTests);
        setPatientInferredObjectProperty(patientInd, properties.treatmentProperty, treatments, patient::getTreatments, patient::setInferredTreatments);
        setPatientInferredObjectProperty(patientInd, properties.causeProperty, causes, patient::getCauses, patient::setInferredCauses);
*/

        return patient;
    }

    private Patient generatePatientFromRule(Rule rule) {
        Matcher diseaseMatcher = diseasePattern.matcher(rule.getName());
        if (diseaseMatcher.find()) {
            // System.out.println(rule);
            String diseaseID = diseaseMatcher.group("diseaseID");
            String number = diseaseMatcher.group("number");
            Map<String, Entity> variables = new HashMap<>();
            Patient patient = parseVariables(rule, diseaseID, number, variables);
            parseRuleBodyAtoms(rule, variables);
            parseRuleHeadAtoms(rule, variables);
            return patient;
        } else
            return null;
    }

    private Patient parseVariables(Rule rule, String diseaseID, String number, Map<String, Entity> variables) {
        Patient patient = null;
        for (AbstractAtom atom : rule.getDeclarationAtoms()) {
            if (atom instanceof ClassDeclarationAtom
                    && ((ClassDeclarationAtom) atom).getArgument() instanceof Variable) {
                Variable var = (Variable) (((ClassDeclarationAtom) atom).getArgument());
                if (((ClassDeclarationAtom) atom).getClassEntity().equals(classes.get("Patient"))) {
                    patient = new Patient(diseaseID + number, diseaseID, number);
                    variables.put(var.getName(), patient);
                } else
                    variables.put(var.getName(), var.getParentClass());
            }
        }

        return patient;
    }

    private void parseRuleBodyAtoms(Rule rule, Map<String, Entity> variables) {
        for (AbstractAtom atom : rule.getBodyAtoms()) {
            if (atom instanceof TwoArgumentsAtom) {
                TwoArgumentsAtom twoArgumentsAtom = (TwoArgumentsAtom) atom;
                String predicate = twoArgumentsAtom.getPredicate();
                switch (predicate) {
                    case "hasSymptom": {
                        addEntityToPatient(twoArgumentsAtom, variables, "addSymptom");
                        break;
                    }
                    case "negativeTest": {
                        addEntityToPatient(twoArgumentsAtom, variables, "addNegativeTest");
                        break;
                    }
                    case "hadOrHasDisease": {
                        addEntityToPatient(twoArgumentsAtom, variables, "addPreviousOrCurrentDisease");
                        break;
                    }
                    case "hasDisease": {
                        addEntityToPatient(twoArgumentsAtom, variables, "addDisease");
                        break;
                    }
                }

            }
        }
        for (AbstractAtom atom : rule.getDeclarationAtoms()) {
            if (atom instanceof TwoArgumentsAtom) {
                TwoArgumentsAtom twoArgumentsAtom = (TwoArgumentsAtom) atom;
                String predicate = twoArgumentsAtom.getPredicate();
                switch (predicate) {
                    case "age": {
                        setPatientAge(rule, twoArgumentsAtom, variables);
                        break;
                    }
                }
            }
        }
    }

    private void parseRuleHeadAtoms(Rule rule, Map<String, Entity> variables) {
        for (AbstractAtom atom : rule.getHeadAtoms()) {
            if (atom instanceof TwoArgumentsAtom) {
                TwoArgumentsAtom twoArgumentsAtom = (TwoArgumentsAtom) atom;
                String predicate = twoArgumentsAtom.getPredicate();
                if (predicate.equals("hasDisease")) {
                    String pName = ((Variable) twoArgumentsAtom.getArgument1()).getName();
                    Patient p = (Patient) variables.get(pName);
                    //TODO
//                    p.addDisease((Entity) twoArgumentsAtom.getArgument2());
                }
            }
        }
    }

    private void addEntityToPatient(TwoArgumentsAtom atom, Map<String, Entity> variables, String patientMethod) {
        try {
            addEntityToPatient(atom, variables, Patient.class.getMethod(patientMethod, Entity.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
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

    private Integer selectNumberFromRange(Range<Integer> range) {
        if (range.hasUpperBound() && range.hasLowerBound()) {
            if (range.upperBoundType() == OPEN && range.lowerBoundType() == OPEN && range.upperEndpoint() - range.lowerEndpoint() <= 1)
                return null;
            if (range.upperEndpoint().equals(range.lowerEndpoint()))
                return range.upperEndpoint();
        }
        int lowerBound = MIN_AGE;
        int upperBound = MAX_AGE;
        if (range.hasUpperBound()) {
            switch (range.upperBoundType()) {
                case OPEN:
                    upperBound = range.upperEndpoint() - 1;
                    break;
                case CLOSED:
                    upperBound = range.upperEndpoint();
            }
        }
        if (range.hasLowerBound()) {
            switch (range.lowerBoundType()) {
                case OPEN:
                    lowerBound = range.lowerEndpoint() + 1;
                    break;
                case CLOSED:
                    lowerBound = range.lowerEndpoint();
            }
        }
        return lowerBound + random.nextInt(upperBound - lowerBound + 1);
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

    private Collection<Entity> getPatientObjectProperties(OWLIndividual patientInd, String propertyName, Map<String, Entity> entities) {
        OWLObjectProperty property = factory.getOWLObjectProperty(propertyName, prefixManager);
        return getObjectPropertyValues(patientInd, property, ontology)
                .stream()
                .map(v -> entities.get(renderer.render(v)))
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    private void setPatientInferredObjectProperty(OWLNamedIndividual patientInd, OWLObjectProperty property,
                                                  Map<String, Entity> entities, Supplier<Collection<Entity>> getter,
                                                  Consumer<Collection<Entity>> setter) {
        Set<Entity> inferredEntities = new HashSet<>();
        for (OWLNamedIndividual entityInd : reasoner.getObjectPropertyValues(patientInd, property)
                .getFlattened()) {
            inferredEntities.add(entities.get(renderer.render(entityInd)));
        }
        inferredEntities.removeAll(getter.get());
        setter.accept(inferredEntities);
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
