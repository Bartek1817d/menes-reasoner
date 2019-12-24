package pl.edu.agh.plonka.bartlomiej.menes.service;

import org.semanticweb.owlapi.model.*;

import static pl.edu.agh.plonka.bartlomiej.menes.utils.Constants.*;

class OntologyProperties {

     final OWLDataProperty firstNameProperty;
     final OWLDataProperty lastNameProperty;
     final OWLDataProperty ageProperty;
     final OWLDataProperty heightProperty;
     final OWLDataProperty weightProperty;

     final OWLObjectProperty symptomProperty;
     final OWLObjectProperty diseaseProperty;
     final OWLObjectProperty testProperty;
     final OWLObjectProperty negativeTestProperty;
     final OWLObjectProperty treatmentProperty;
     final OWLObjectProperty causeProperty;
     final OWLObjectProperty previousOrCurrentDiseaseProperty;

     final OWLClass diseaseClass;
     final OWLClass symptomClass;
     final OWLClass causeClass;
     final OWLClass treatmentClass;
     final OWLClass testingClass;
     final OWLClass patientClass;

     OntologyProperties(OWLDataFactory factory, PrefixManager prefixManager) {
         firstNameProperty = factory.getOWLDataProperty(FIRST_NAME_PROPERTY, prefixManager);
         lastNameProperty = factory.getOWLDataProperty(LAST_NAME_PROPERTY, prefixManager);
         ageProperty = factory.getOWLDataProperty(AGE_PROPERTY, prefixManager);
         heightProperty = factory.getOWLDataProperty(HEIGHT_PROPERTY, prefixManager);
         weightProperty = factory.getOWLDataProperty(WEIGHT_PROPERTY, prefixManager);
         symptomProperty = factory.getOWLObjectProperty(HAS_SYMPTOM_PROPERTY, prefixManager);
         diseaseProperty = factory.getOWLObjectProperty(HAS_DISEASE_PROPERTY, prefixManager);
         testProperty = factory.getOWLObjectProperty(SHOULD_MAKE_TEST_PROPERTY, prefixManager);
         negativeTestProperty = factory.getOWLObjectProperty(NEGATIVE_TEST_PROPERTY, prefixManager);
         treatmentProperty = factory.getOWLObjectProperty(SHOULD_BE_TREATED_WITH_PROPERTY, prefixManager);
         causeProperty = factory.getOWLObjectProperty(CAUSE_OF_DISEASE_PROPERTY, prefixManager);
         previousOrCurrentDiseaseProperty = factory.getOWLObjectProperty(PREVIOUS_DISEASE_PROPERTY, prefixManager);

         diseaseClass = factory.getOWLClass(DISEASE_CLASS, prefixManager);
         symptomClass = factory.getOWLClass(SYMPTOM_CLASS, prefixManager);
         causeClass = factory.getOWLClass(CAUSE_CLASS, prefixManager);
         treatmentClass = factory.getOWLClass(TREATMENT_CLASS, prefixManager);
         testingClass = factory.getOWLClass(TESTING_CLASS, prefixManager);
         patientClass = factory.getOWLClass(PATIENT_CLASS, prefixManager);
     }
}
