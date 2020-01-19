package pl.edu.agh.plonka.bartlomiej.menes.service;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.PrefixManager;

import static pl.edu.agh.plonka.bartlomiej.menes.utils.Constants.PATIENT_CLASS;

class OntologyProperties {

     final OWLClass patientClass;

     OntologyProperties(OWLDataFactory factory, PrefixManager prefixManager) {
         patientClass = factory.getOWLClass(PATIENT_CLASS, prefixManager);
     }
}
