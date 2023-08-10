package com.example.function.analogs;

import com.example.util.KieUtil;
import com.example.vo.AnalogData;
import com.example.vo.AnalogDataAccumulator;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.kie.api.runtime.KieSession;

/**
 * 模拟量对象的窗口聚集函数
 */
public class AnalogAggregateFunction implements AggregateFunction<AnalogData, AnalogDataAccumulator, AnalogDataAccumulator> {

    // 聚集函数的实现类需要静态化处理，否则会报错：The implementation of the AggregateFunction is not serializable.
    private static volatile AnalogAggregateFunction avgAggregateFunction;

    // 构造器私有化
    private AnalogAggregateFunction() {}

    // 懒汉式获取本类对象，确保全局内只有一个对象被创建
    public static AnalogAggregateFunction getInstance() {
        if (avgAggregateFunction == null) {
            synchronized (AnalogAggregateFunction.class) {
                if (avgAggregateFunction == null) {
                    avgAggregateFunction = new AnalogAggregateFunction();
                }
            }
        }
        return avgAggregateFunction;
    }

    @Override
    public AnalogDataAccumulator createAccumulator() {
        return new AnalogDataAccumulator();
    }

    @Override
    public AnalogDataAccumulator add(AnalogData value, AnalogDataAccumulator accumulator) {
        accumulator.add(value);

        // 分发给规则引擎进行规则处理
        KieSession kieSession = KieUtil.kieContainer.newKieSession();
        kieSession.insert(accumulator);
        kieSession.fireAllRules();
        //kieSession.dispose();

        return accumulator;
    }

    @Override
    public AnalogDataAccumulator getResult(AnalogDataAccumulator accumulator) {
        return accumulator;
    }

    // merge 适用于会话窗口，此处用不上
    @Override
    public AnalogDataAccumulator merge(AnalogDataAccumulator a, AnalogDataAccumulator b) {
        return null;
    }
}