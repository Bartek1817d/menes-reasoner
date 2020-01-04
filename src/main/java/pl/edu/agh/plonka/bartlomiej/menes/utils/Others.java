package pl.edu.agh.plonka.bartlomiej.menes.utils;

import pl.edu.agh.plonka.bartlomiej.menes.model.Entity;

import java.util.Collection;

public class Others {

    public static <T extends Entity> boolean containsEntity(String id, Collection<T> entities) {
        return entities
                .stream()
                .anyMatch(e -> e.getID().equals(id));
    }

    public static <T extends Entity> T findEntity(String id, Collection<T> entities) {
        return entities
                .stream()
                .filter(e -> e.getID().equals(id))
                .findAny()
                .orElse(null);
    }

}
