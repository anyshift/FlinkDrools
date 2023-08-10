package com.example.function.switchs;

import com.example.util.KieUtil;
import com.example.vo.SwitchData;
import com.example.vo.SwitchDataAccumulator;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.kie.api.runtime.KieSession;

/**
 * 开关量对象的窗口聚集函数
 */
public class SwitchAggregateFunction implements AggregateFunction<SwitchData, SwitchDataAccumulator, SwitchDataAccumulator> {

    // 聚集函数的实现类需要静态化处理，否则会报错：The implementation of the AggregateFunction is not serializable.
    private static volatile SwitchAggregateFunction switchAggregateFunction;

    // 构造器私有化
    private SwitchAggregateFunction() {}

    // 懒汉式获取本类对象，确保全局内只有一个对象被创建
    public static SwitchAggregateFunction getInstance() {
        if (switchAggregateFunction == null) {
            synchronized (SwitchAggregateFunction.class) {
                if (switchAggregateFunction == null) {
                    switchAggregateFunction = new SwitchAggregateFunction();
                }
            }
        }
        return switchAggregateFunction;
    }

    @Override
    public SwitchDataAccumulator createAccumulator() {
        return new SwitchDataAccumulator();
    }

    @Override
    public SwitchDataAccumulator add(SwitchData value, SwitchDataAccumulator accumulator) {
        accumulator.add(value);

        // 分发给规则引擎进行规则处理
        KieSession kieSession = KieUtil.kieContainer.newKieSession();
        kieSession.insert(accumulator);
        kieSession.fireAllRules();
        //kieSession.dispose();

        return accumulator;
    }

    @Override
    public SwitchDataAccumulator getResult(SwitchDataAccumulator accumulator) {
        return accumulator;
    }

    @Override
    public SwitchDataAccumulator merge(SwitchDataAccumulator a, SwitchDataAccumulator b) {
        return null;
    }
}
