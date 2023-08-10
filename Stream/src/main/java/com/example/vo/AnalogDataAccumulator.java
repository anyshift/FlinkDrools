package com.example.vo;

import lombok.Setter;
import lombok.ToString;

/**
 * 模拟量对象的累加器对象
 */
@Setter
@ToString
public class AnalogDataAccumulator {

    private Double maxValue = 0.0;

    private Double minValue = 0.0;

    private Double sum = 0.0;

    private Long count = 0L;

    private AnalogData analogData;

    private static volatile AnalogDataAccumulator accumulator;

    //private AnalogDataAccumulator() {}

    public static AnalogDataAccumulator getInstance() {
        if (accumulator == null) {
            synchronized (AnalogDataAccumulator.class) {
                if (accumulator == null) {
                    accumulator = new AnalogDataAccumulator();
                }
            }
        }
        return accumulator;
    }

    public void add(AnalogData data) {
        this.analogData = data;
        maxValue = (maxValue == 0) ? data.getValue() : (Math.max(maxValue, data.getValue()));
        minValue = (minValue == 0) ? data.getValue() : (Math.min(minValue, data.getValue()));
        sum += data.getValue();
        count++;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public Double getMinValue() {
        return minValue;
    }

    public Double getAvgValue() {
        return sum / count;
    }

    public AnalogData getAnalogData() {
        return analogData;
    }
}