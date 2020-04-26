package pl.edu.agh.plonka.bartlomiej.menes.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static java.util.Collections.disjoint;

public class Property extends Entity {

    private static final Collection<String> INTEGER_DATA_TYPES = Arrays.asList("unsignedByte", "decimal", "double", "float");

    public Property(String id) {
        super(id);
    }

    public static boolean isNumericProperty(Set<String> rangeTypes) {
        return !disjoint(rangeTypes, INTEGER_DATA_TYPES);
    }

    public static boolean isBooleanProperty(Set<String> rangeTypes) {
        return rangeTypes.size() == 1 && rangeTypes.contains("boolean");
    }

}
