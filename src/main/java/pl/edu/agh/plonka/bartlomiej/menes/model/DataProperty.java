package pl.edu.agh.plonka.bartlomiej.menes.model;

import java.util.Arrays;
import java.util.Collection;

import static java.util.Collections.disjoint;

public class DataProperty extends Property<String> {

    private static final Collection<String> INTEGER_DATA_TYPES = Arrays.asList("unsignedByte");

    public DataProperty() {
    }

    public DataProperty(String id) {
        super(id);
    }

    public boolean isStringProperty() {
        return disjoint(ranges, INTEGER_DATA_TYPES);
    }

    public boolean isIntegerProperty() {
        return !isStringProperty();
    }

}
