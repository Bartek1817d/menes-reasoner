package pl.edu.agh.plonka.bartlomiej.menes.model;

public class IntegerProperty extends Property {

    private Integer minValue;
    private Integer maxValue;

    public IntegerProperty() {
    }

    public IntegerProperty(String id) {
        super(id);
    }

    public IntegerProperty(String id, Integer maxValue, Integer minValue) {
        super(id);
        this.maxValue = maxValue;
        this.minValue = minValue;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }
}
