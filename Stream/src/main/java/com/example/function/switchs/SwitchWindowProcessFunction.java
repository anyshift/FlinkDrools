package com.example.function.switchs;

import com.example.vo.SwitchDataAccumulator;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * 开关量对象的窗口处理函数
 */
public class SwitchWindowProcessFunction extends ProcessWindowFunction<SwitchDataAccumulator, String, String, TimeWindow> {

    private static volatile SwitchWindowProcessFunction switchWindowProcessFunction;

    private SwitchWindowProcessFunction() {}

    public static SwitchWindowProcessFunction getInstance() {
        if (switchWindowProcessFunction == null) {
            synchronized (SwitchWindowProcessFunction.class) {
                if (switchWindowProcessFunction == null) {
                    switchWindowProcessFunction = new SwitchWindowProcessFunction();
                }
            }
        }
        return switchWindowProcessFunction;
    }

    @Override
    public void process(String key, ProcessWindowFunction<SwitchDataAccumulator, String, String, TimeWindow>.Context context, Iterable<SwitchDataAccumulator> elements, Collector<String> out) throws Exception {
        String windowStartTime = DateFormatUtils.format(context.window().getStart(), "HH:mm:ss");
        String windowEndTime = DateFormatUtils.format(context.window().getEnd(), "HH:mm:ss");

        // 聚合函数在一个窗口时间内处理结果后，会把累加器的结果存到迭代器中
        SwitchDataAccumulator accumulator = elements.iterator().next();
        String result = String.format("设备【%s】, 离线次数【%d】, 时间窗口【%s --> %s】",
                key, accumulator.getOffCount(), windowStartTime, windowEndTime);

        // 采集器采集结果
        out.collect(result);
    }
}
