package pl.edu.agh.plonka.bartlomiej.menes.model.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.plonka.bartlomiej.menes.model.Patient;

import java.util.Collection;
import java.util.Comparator;

public class ComplexComparator implements Comparator<Complex> {

    private final static Logger LOG = LoggerFactory.getLogger(ComplexComparator.class);

    private final static float w1 = 1f;
    private final static float w2 = 1f;

    public static void sortStar(Star star, Category category, Collection<Patient> trainingSet) {
        ComplexComparator comparator = new ComplexComparator(star, category, trainingSet);
        star.sort(comparator.reversed());
    }

    private ComplexComparator(Star star, Category category, Collection<Patient> trainingSet) {
        star.forEach(complex -> evaluateComplex(complex, category, trainingSet));
    }

    private static void evaluateComplex(Complex complex, Category category, Collection<Patient> trainingSet) {
        int coveredWithTheSameCategory = 0;
        int uncoveredWithDifferentCategory = 0;

        for (Patient patient : trainingSet) {
            if (complex.isPatientCovered(patient) && category.assertPatientInCategory(patient))
                coveredWithTheSameCategory++;
            else if (!complex.isPatientCovered(patient) && !category.assertPatientInCategory(patient)) {
                uncoveredWithDifferentCategory++;
            }
        }

        complex.setEvaluation(w1 * coveredWithTheSameCategory + w2 * uncoveredWithDifferentCategory);
    }

    @Override
    public int compare(Complex complex1, Complex complex2) {
        if (complex1.getEvaluation() < complex2.getEvaluation()) {
            return -1;
        } else if (complex1.getEvaluation() > complex2.getEvaluation()) {
            return 1;
        } else {
            return 0;
        }
    }


}
