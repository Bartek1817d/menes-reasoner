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
import static pl.edu.agh.plonka.bartlomiej.menes.model.Property.isIntegerProperty;

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

    public Map<String, OntologyClass> loadClasses() {
        Map<String, OntologyClass> classes = new HashMap<>();
        for (OWLClass owlClass : ontology.getClassesInSignature()) {
            OntologyClass classEntity = loadClass(owlClass, classes);

            for (OWLClassExpression owlSuperClass : EntitySearcher.getSuperClasses(owlClass, ontology)) {
                if (owlSuperClass.isAnonymous())
                    continue;
                Entity superClassEntity = loadClass(owlSuperClass.asOWLClass(), classes);
                classEntity.addClass(superClassEntity);
            }
        }
        return classes;
    }

    public Map<String, Entity> loadInstances(Map<String, OntologyClass> classes) {
        Map<String, Entity> instances = new HashMap<>();
        for (OWLNamedIndividual owlInstance : ontology.getIndividualsInSignature()) {
            loadInstance(owlInstance, instances, classes);
        }
        return instances;
    }

    public Set<IntegerProperty> loadIntegerProperties() {
        return ontology.getDataPropertiesInSignature()
                .stream()
                .map(this::loadIntegerProperty)
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    public Set<Property> loadStringProperties() {
        return ontology.getDataPropertiesInSignature()
                .stream()
                .map(this::loadStringProperty)
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    public Set<ObjectProperty> loadObjectProperties(Map<String, OntologyClass> classes) {
        return ontology.getObjectPropertiesInSignature()
                .stream()
                .map(p -> loadObjectProperty(p, classes))
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    private IntegerProperty loadIntegerProperty(OWLDataProperty owlDataProperty) {
        String propertyName = renderer.render(owlDataProperty);
        if (!validateDataProperty(propertyName, owlDataProperty))
            return null;
        Set<String> rangeTypes = getDataPropertyRangeTypes(owlDataProperty);
        if (!isIntegerProperty(rangeTypes))
            return null;
        return new IntegerProperty(propertyName);
    }

    private Property loadStringProperty(OWLDataProperty owlDataProperty) {
        String propertyName = renderer.render(owlDataProperty);
        if (!validateDataProperty(propertyName, owlDataProperty))
            return null;
        Set<String> rangeTypes = getDataPropertyRangeTypes(owlDataProperty);
        if (isIntegerProperty(rangeTypes))
            return null;
        return new Property(propertyName);
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

    private Set<OntologyClass> getObjectPropertyRangeTypes(OWLObjectProperty owlObjectProperty, Map<String, OntologyClass> classes) {
        return ontology.getObjectPropertyRangeAxioms(owlObjectProperty)
                .stream()
                .map(OWLPropertyRangeAxiom::getRange)
                .map(renderer::render)
                .map(classes::get)
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    private ObjectProperty loadObjectProperty(OWLObjectProperty owlObjectProperty, Map<String, OntologyClass> classes) {
        String propertyName = renderer.render(owlObjectProperty);
        if (!validateObjectProperty(propertyName, owlObjectProperty))
            return null;

        Set<OntologyClass> rangeTypes = getObjectPropertyRangeTypes(owlObjectProperty, classes);

        ObjectProperty property = new ObjectProperty(propertyName);
        property.setRanges(rangeTypes);
        return property;
    }


    private OntologyClass loadClass(OWLEntity owlClass, Map<String, OntologyClass> classes) {
        String classID = renderer.render(owlClass);
        OntologyClass classEntity = classes.get(classID);
        if (classEntity == null) {
            classEntity = new OntologyClass(classID);
            classes.put(classID, classEntity);
        }

        if (classEntity.getLabel() == null) {
            classEntity.setLanguageLabelMap(getLabel(owlClass));
            classEntity.setLanguageCommentMap(getComment(owlClass));
            classEntity.setLanguage();
        }

        return classEntity;
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

    private Entity loadInstance(OWLNamedIndividual owlInstance, Map<String, Entity> instances, Map<String, OntologyClass> classes) {
        String instanceID = renderer.render(owlInstance);
        Entity instance = new Entity(instanceID);
        for (OWLClassExpression owlParentClass : EntitySearcher.getTypes(owlInstance, ontology)) {
            OntologyClass cls = classes.get(renderer.render(owlParentClass));
            cls.addInstance(instance);
            instance.addClass(cls);
        }
        instance.setLanguageLabelMap(getLabel(owlInstance));
        instance.setLanguageCommentMap(getComment(owlInstance));
        instance.setLanguage();
        instances.put(instanceID, instance);
        return instance;
    }
}


