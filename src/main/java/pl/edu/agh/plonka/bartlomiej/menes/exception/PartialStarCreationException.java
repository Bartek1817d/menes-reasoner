package pl.edu.agh.plonka.bartlomiej.menes.exception;

import pl.edu.agh.plonka.bartlomiej.menes.model.Patient;

import static java.lang.String.format;

public class PartialStarCreationException extends Exception {
    public PartialStarCreationException(Patient positivePatient, Patient negativePatient) {
        super(format("ERROR_CREATING_PARTIAL_STAR_EXCEPTION %s %s", positivePatient, negativePatient));
    }
}
