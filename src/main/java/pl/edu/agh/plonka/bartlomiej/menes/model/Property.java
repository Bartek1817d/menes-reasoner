package pl.edu.agh.plonka.bartlomiej.menes.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Property<T> extends Entity {

    protected final Set<T> ranges = new HashSet<>();

    public Property() {
    }

    public Property(String id) {
        super(id);
    }

    public Set<T> getRanges() {
        return ranges;
    }

    public void setRanges(Collection<T> ranges) {
        this.ranges.clear();
        this.ranges.addAll(ranges);
    }

    public void addRanges(Collection<T> ranges) {
        this.ranges.addAll(ranges);
    }

    public void addRange(T range) {
        this.ranges.add(range);
    }


}
