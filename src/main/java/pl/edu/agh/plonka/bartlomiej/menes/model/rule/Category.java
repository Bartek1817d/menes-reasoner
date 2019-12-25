package pl.edu.agh.plonka.bartlomiej.menes.model.rule;

import pl.edu.agh.plonka.bartlomiej.menes.model.Entity;
import pl.edu.agh.plonka.bartlomiej.menes.model.Patient;

public class Category {

    public enum Predicate {
        HAS_DISEASE,
        SHOULD_MAKE_TEST,
        SHOULD_BE_TREATED_WITH,
        CAUSE_OF_DISEASE
    }

    private Entity entity;
    private Predicate predicate;

    public Category(Entity entity, Predicate predicate) {
//        Assert.notNull(entity, "Entity required.");
//        Assert.notNull(entity, "Predicate required.");

        this.entity = entity;
        this.predicate = predicate;
    }

    public boolean assertPatientInCategory(Patient patient) {
        //TODO
        switch (predicate) {
//            case HAS_DISEASE:
//                return patient.getDiseases().contains(entity) || patient.getInferredDiseases().contains(entity);
//            case CAUSE_OF_DISEASE:
//                return patient.getCauses().contains(entity) || patient.getInferredCauses().contains(entity);
//            case SHOULD_MAKE_TEST:
//                return patient.getTests().contains(entity) || patient.getInferredTests().contains(entity);
//            case SHOULD_BE_TREATED_WITH:
//                return patient.getTreatments().contains(entity) || patient.getInferredTreatments().contains(entity);
            default:
                return false;
        }
    }

    public void setPatientCategory(Patient patient) {
        //TODO
//        switch (predicate) {
//            case HAS_DISEASE:
//                patient.addDisease(entity);
//                break;
//            case CAUSE_OF_DISEASE:
//                patient.addCause(entity);
//                break;
//            case SHOULD_MAKE_TEST:
//                patient.addTest(entity);
//                break;
//            case SHOULD_BE_TREATED_WITH:
//                patient.addTreatment(entity);
//                break;
//        }
    }

    public Entity getEntity() {
        return entity;
    }

    public Predicate getPredicate() {
        return predicate;
    }
}
