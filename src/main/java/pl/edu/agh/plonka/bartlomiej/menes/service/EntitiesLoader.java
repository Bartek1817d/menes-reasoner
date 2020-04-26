package pl.edu.agh.plonka.bartlomiej.menes.service;

import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import pl.edu.agh.plonka.bartlomiej.menes.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;
import static pl.edu.agh.plonka.bartlomiej.menes.model.Property.isBooleanProperty;
import static pl.edu.agh.plonka.bartlomiej.menes.model.Property.isNumericProperty;
import static pl.edu.agh.plonka.bartlomiej.menes.utils.Others.findEntity;

class EntitiesLoader {

    private static final Logger LOG = getLogger(EntitiesLoader.class);

    private final OWLOntology ontology;
    private final OWLObjectRenderer renderer;
    private final OWLDataFactory factory;
    private final OWLReasoner reasoner;
    private final OntologyProperties ontologyProperties;

    EntitiesLoader(OWLOntology ontology, OWLObjectRenderer renderer, OWLDataFactory factory, OWLReasoner reasoner, OntologyProperties ontologyProperties) {
        this.ontology = ontology;
        this.renderer = renderer;
        this.factory = factory;
        this.reasoner = reasoner;
        this.ontologyProperties = ontologyProperties;
    }

    public Set<OntologyClass> loadClasses() {
        return ontology.getClassesInSignature()
                .stream()
                .map(this::createClass)
                .collect(toSet());
    }

    public Set<Entity> loadInstances(Set<OntologyClass> classes) {
        return ontology.getIndividualsInSignature()
                .stream()
                .map(individual -> loadInstance(individual, classes))
                .collect(toSet());
    }

    public Properties loadProperties(Set<OntologyClass> classes) {
        Properties properties = new Properties();
        ontology.getObjectPropertiesInSignature().forEach(property -> fillProperties(property, classes, properties));
        ontology.getDataPropertiesInSignature().forEach(property -> fillProperties(property, properties));
        return properties;
    }

    private void fillProperties(OWLObjectProperty owlProperty, Set<OntologyClass> classes, Properties properties) {
        String propertyName = renderer.render(owlProperty);
        if (!validateObjectProperty(propertyName, owlProperty))
            return;

        Set<OntologyClass> rangeTypes = getObjectPropertyRangeTypes(owlProperty, classes);

        ObjectProperty property = new ObjectProperty(propertyName);
        property.setRanges(rangeTypes);

        properties.entityProperties.add(property);
    }

    private void fillProperties(OWLDataProperty owlProperty, Properties properties) {
        String propertyName = renderer.render(owlProperty);
        if (!validateDataProperty(propertyName, owlProperty))
            return;
        Set<String> rangeTypes = getDataPropertyRangeTypes(owlProperty);
        if (isNumericProperty(rangeTypes))
            properties.numericProperties.add(new NumericProperty(propertyName));
        else if (isBooleanProperty(rangeTypes))
            properties.booleanProperties.add(new Property(propertyName));
        else
            properties.stringProperties.add(new Property(propertyName));
    }

    private boolean validateDataProperty(String propertyName, OWLDataProperty owlDataProperty) {
        if (!reasoner.getDataPropertyDomains(owlDataProperty, false).containsEntity(ontologyProperties.patientClass)) {
            LOG.warn("{} doesn't have 'Patient' domain.", propertyName);
            return false;
        }
        if (!reasoner.getSubDataProperties(owlDataProperty, false).isSingleton()) {
            LOG.warn("{} isn't bottom property.", propertyName);
            return false;
        }
        return true;
    }

    private boolean validateObjectProperty(String propertyName, OWLObjectProperty owlObjectProperty) {
        if (!reasoner.getObjectPropertyDomains(owlObjectProperty, false).containsEntity(ontologyProperties.patientClass)) {
            LOG.warn("{} doesn't have 'Patient' domain.", propertyName);
            return false;
        }
        if (!reasoner.getSubObjectProperties(owlObjectProperty, false).isSingleton()) {
            LOG.warn("{} isn't bottom property.", propertyName);
            return false;
        }
        return true;
    }

    private Set<String> getDataPropertyRangeTypes(OWLDataProperty owlDataProperty) {
        return ontology.getDataPropertyRangeAxioms(owlDataProperty)
                .stream()
                .map(OWLPropertyRangeAxiom::getRange)
                .map(renderer::render)
                .collect(toSet());
    }

    private Set<OntologyClass> getObjectPropertyRangeTypes(OWLObjectProperty owlObjectProperty, Set<OntologyClass> classes) {
        return ontology.getObjectPropertyRangeAxioms(owlObjectProperty)
                .stream()
                .map(OWLPropertyRangeAxiom::getRange)
                .map(renderer::render)
                .map(id -> findEntity(id, classes))
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    private Map<String, String> getLabel(OWLEntity owlClass) {
        return getProperty(owlClass, factory.getRDFSLabel());
    }

    private Map<String, String> getComment(OWLEntity owlClass) {
        return getProperty(owlClass, factory.getRDFSComment());
    }

    private Map<String, String> getProperty(OWLEntity owlClass, OWLAnnotationProperty annotationProperty) {
        HashMap<String, String> propertyMap = new HashMap<>();
        for (OWLAnnotation annotation : EntitySearcher.getAnnotations(owlClass, ontology,
                annotationProperty)) {
            OWLAnnotationValue val = annotation.getValue();
            if (val instanceof OWLLiteral) {
                OWLLiteral label = (OWLLiteral) val;
                propertyMap.put(label.getLang(), label.getLiteral());
            }
        }
        return propertyMap;
    }

    private Entity loadInstance(OWLNamedIndividual owlInstance, Set<OntologyClass> classes) {
        String instanceID = renderer.render(owlInstance);
        Entity instance = new Entity(instanceID);

        reasoner.getTypes(owlInstance, false).getFlattened()
                .stream()
                .map(owlClass -> getOrCreateClass(owlClass, classes))
                .forEach(type -> {
                    type.addInstance(instance);
                    instance.addClass(type);
                });

        instance.setLanguageLabelMap(getLabel(owlInstance));
        instance.setLanguageCommentMap(getComment(owlInstance));
        instance.setLanguage();
        return instance;
    }

    private OntologyClass getOrCreateClass(OWLClass owlClass, Set<OntologyClass> classes) {
        String classID = renderer.render(owlClass);
        OntologyClass classEntity = findEntity(classID, classes);
        if (classEntity == null) {
            classEntity = createClass(owlClass);
            classes.add(classEntity);
        }
        return classEntity;
    }

    private OntologyClass createClass(OWLClass owlClass) {
        String classID = renderer.render(owlClass);
        OntologyClass classEntity = new OntologyClass(classID);
        classEntity.setLanguageLabelMap(getLabel(owlClass));
        classEntity.setLanguageCommentMap(getComment(owlClass));
        classEntity.setLanguage();
        return classEntity;
    }
}


