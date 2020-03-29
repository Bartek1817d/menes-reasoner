package pl.edu.agh.plonka.bartlomiej.menes.model;

public class NumericProperty extends Property {

    private Float minValue;
    private Float maxValue;

    public NumericProperty(String id) {
        super(id);
    }

    public NumericProperty(String id, Float maxValue, Float minValue) {
        super(id);
        this.maxValue = maxValue;
        this.minValue = minValue;
    }

    public Float getMinValue() {
        return minValue;
    }

    public void setMinValue(Float minValue) {
        this.minValue = minValue;
    }

    public Float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Float maxValue) {
        this.maxValue = maxValue;
    }
}
