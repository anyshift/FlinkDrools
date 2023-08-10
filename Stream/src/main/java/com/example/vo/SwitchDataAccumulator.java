package com.example.vo;

import lombok.Setter;
import lombok.ToString;

/**
 * 开关量对象的累加器对象
 */
@Setter
@ToString
public class SwitchDataAccumulator {

    private Long offCount = 0L;

    private Long count = 0L;

    private SwitchData switchData;

    private static volatile SwitchDataAccumulator accumulator;

    //private SwitchDataAccumulator() {}

    public static SwitchDataAccumulator getInstance() {
        if (accumulator == null) {
            synchronized (SwitchDataAccumulator.class) {
                if (accumulator == null) {
                    accumulator = new SwitchDataAccumulator();
                }
            }
        }
        return accumulator;
    }

    public void add(SwitchData switchData) {
        this.switchData = switchData;
        count++;
        if (switchData.getStatus() == Boolean.FALSE) offCount++;
    }

    public Long getOffCount() {
        return offCount;
    }

    public Long getCount() {
        return count;
    }

    public SwitchData getSwitchData() {
        return switchData;
    }
}
