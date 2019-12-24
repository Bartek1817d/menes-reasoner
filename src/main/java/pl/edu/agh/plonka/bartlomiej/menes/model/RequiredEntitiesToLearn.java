package pl.edu.agh.plonka.bartlomiej.menes.model;

public class RequiredEntitiesToLearn {
    public boolean diseases;
    public boolean tests;
    public boolean treatments;
    public boolean causes;

    public RequiredEntitiesToLearn(boolean diseases, boolean tests, boolean treatments, boolean causes) {
        this.diseases = diseases;
        this.tests = tests;
        this.treatments = treatments;
        this.causes = causes;
    }

    public boolean invalidPatient(Patient patient) {
        return ((diseases && patient.getInferredDiseases().isEmpty() && patient.getDiseases().isEmpty()) ||
                (tests && patient.getInferredTests().isEmpty() && patient.getTests().isEmpty()) ||
                (treatments && patient.getInferredTreatments().isEmpty() && patient.getTreatments().isEmpty()) ||
                (causes && patient.getInferredCauses().isEmpty()) && patient.getCauses().isEmpty());

    }
}
