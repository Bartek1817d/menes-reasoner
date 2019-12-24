package pl.edu.agh.plonka.bartlomiej.menes.model.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.plonka.bartlomiej.menes.model.Patient;

import java.util.ArrayList;
import java.util.Collection;

public class Star extends ArrayList<Complex> {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    public Star() {
        add(new Complex());
    }

    public boolean isPatientCovered(Patient patient) {
        for (Complex complex : this) {
            if (complex.isPatientCovered(patient))
                return true;
        }
        return false;
    }

    public void intersection(Collection<Complex> otherComplexes) {
        if (isEmpty())
            addAll(otherComplexes);
        Collection<Complex> newComplexes = Complex.intersection(this, otherComplexes);
        //TODO check if it's ok
        clear();
        addAll(newComplexes);
    }

    public void deleteNarrowComplexes() {
        Collection<Complex> toRemove = new ArrayList<>();
        for (Complex c1 : this)
            for (Complex c2 : this)
                if (c1 != c2 && c1.contains(c2))
                    toRemove.add(c2);
        removeAll(toRemove);
    }

    public void leaveFirstElements(int n) {
        subList(Math.min(n, size()), size()).clear();
    }

}
