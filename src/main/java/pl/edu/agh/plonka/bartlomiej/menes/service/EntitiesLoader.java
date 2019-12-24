package pl.edu.agh.plonka.bartlomiej.menes.service;

import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;
import pl.edu.agh.plonka.bartlomiej.menes.model.Entity;

import java.util.HashMap;
import java.util.Map;

class EntitiesLoader {

    private final OWLOntology ontology;
    private final OWLObjectRenderer renderer;
    private final OWLDataFactory factory;
    private final OWLReasoner reasoner;

    EntitiesLoader(OWLOntology ontology, OWLObjectRenderer renderer, OWLDataFactory factory, OWLReasoner reasoner) {
        this.ontology = ontology;
        this.renderer = renderer;
        this.factory = factory;
        this.reasoner = reasoner;
    }

    Map<String, Entity> loadClasses() {
        Map<String, Entity> classes = new HashMap<>();
        for (OWLClass owlClass : ontology.getClassesInSignature()) {
            Entity classEntity = loadClass(owlClass, classes);

            for (OWLClassExpression owlSuperClass : EntitySearcher.getSuperClasses(owlClass, ontology)) {
                if (owlSuperClass.isAnonymous())
                    continue;
                Entity superClassEntity = loadClass(owlSuperClass.asOWLClass(), classes);
                classEntity.addClass(superClassEntity);
            }
        }
        return classes;
    }

    Map<String, Entity> loadInstances(OWLClass owlClass, Map<String, Entity> classes) {
        Map<String, Entity> instances = new HashMap<>();
        for (OWLNamedIndividual owlInstance : reasoner.getInstances(owlClass, false).getFlattened()) {
            loadInstance(owlInstance, instances, classes);
        }
        return instances;
    }

    private Entity loadClass(OWLEntity owlClass, Map<String, Entity> classes) {
        String classID = renderer.render(owlClass);
        Entity classEntity = classes.get(classID);
        if (classEntity == null) {
            classEntity = new Entity(classID);
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

    private Entity loadInstance(OWLNamedIndividual owlInstance, Map<String, Entity> instances, Map<String, Entity> classes) {
        String instanceID = renderer.render(owlInstance);
        Entity instance = new Entity(instanceID);
        for (OWLClassExpression owlParentClass : EntitySearcher.getTypes(owlInstance, ontology))
            instance.addClass(classes.get(renderer.render(owlParentClass)));
        instance.setLanguageLabelMap(getLabel(owlInstance));
        instance.setLanguageCommentMap(getComment(owlInstance));
        instance.setLanguage();
        instances.put(instanceID, instance);
        return instance;
    }
}


