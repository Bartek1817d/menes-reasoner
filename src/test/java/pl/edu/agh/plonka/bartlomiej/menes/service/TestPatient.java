package pl.edu.agh.plonka.bartlomiej.menes.service;

import pl.edu.agh.plonka.bartlomiej.menes.model.Entity;
import pl.edu.agh.plonka.bartlomiej.menes.model.ObjectProperty;
import pl.edu.agh.plonka.bartlomiej.menes.model.Patient;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TestPatient {

    private final Patient patient;
    private final Map<String, Set<Entity>> trueEntityCategoryValues = new HashMap<>();

    public TestPatient(Patient patient, Set<ObjectProperty> categories) {
        this.patient = patient;
        copyCategoriesValues(categories);
        removeCategoryValues(categories);
    }

    private void copyCategoriesValues(Set<ObjectProperty> categories) {
        categories.forEach(category -> {
            String categoryId = category.getID();
            Set<Entity> categoryValues = patient.getEntityProperties(categoryId);
            if (categoryValues != null && !categoryValues.isEmpty()) {
                trueEntityCategoryValues.put(
                        categoryId,
                        categoryValues
                );
            }
        });
    }

    private void removeCategoryValues(Set<ObjectProperty> categories) {
        categories.forEach(category -> patient.clearEntityProperties(category.getID()));
    }

    public Patient getPatient() {
        return patient;
    }

    public boolean invalidPatient() {
        return !validPatient();
    }

    public boolean validPatient() {
        return trueEntityCategoryValues
                .entrySet()
                .stream()
                .allMatch(category ->
                        {
                            Set<Entity> inferredProperties = patient.getInferredEntityProperties(category.getKey());
                            if (inferredProperties == null)
                                return false;
                            return inferredProperties
                                    .containsAll(category.getValue());
                        }
                );
    }
}
