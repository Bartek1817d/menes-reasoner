package pl.edu.agh.plonka.bartlomiej.menes.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ObjectProperty extends Property {

    private final Set<OntologyClass> ranges = new HashSet<>();

    public ObjectProperty() {
    }

    public ObjectProperty(String id) {
        super(id);
    }

    public Set<OntologyClass> getRanges() {
        return ranges;
    }

    public void setRanges(Collection<OntologyClass> ranges) {
        this.ranges.clear();
        this.ranges.addAll(ranges);
    }

    public void addRanges(Collection<OntologyClass> ranges) {
        this.ranges.addAll(ranges);
    }

    public void addRange(OntologyClass range) {
        this.ranges.add(range);
    }
}
