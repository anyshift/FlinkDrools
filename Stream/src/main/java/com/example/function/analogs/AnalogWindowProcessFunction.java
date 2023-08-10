package com.example.function.analogs;

import com.example.vo.AnalogDataAccumulator;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * 模拟量对象的窗口处理函数
 */
public class AnalogWindowProcessFunction extends ProcessWindowFunction<AnalogDataAccumulator, String, String, TimeWindow> {

    private static volatile AnalogWindowProcessFunction analogWindowProcessFunction;

    private AnalogWindowProcessFunction() {}

    public static AnalogWindowProcessFunction getInstance() {
        if (analogWindowProcessFunction == null) {
            synchronized (AnalogWindowProcessFunction.class) {
                if (analogWindowProcessFunction == null) {
                    analogWindowProcessFunction = new AnalogWindowProcessFunction();
                }
            }
        }
        return analogWindowProcessFunction;
    }

    @Override
    public void process(String key, ProcessWindowFunction<AnalogDataAccumulator, String, String, TimeWindow>.Context context, Iterable<AnalogDataAccumulator> elements, Collector<String> out) throws Exception {
        String windowStartTime = DateFormatUtils.format(context.window().getStart(), "HH:mm:ss");
        String windowEndTime = DateFormatUtils.format(context.window().getEnd(), "HH:mm:ss");

        // 聚合函数在一个窗口时间内处理结果后，会把累加器的结果存到迭代器中
        AnalogDataAccumulator accumulator = elements.iterator().next();
        String result = String.format("设备【%s】, 最大值【%f】, 最小值【%f】, 平均值【%f】, 时间窗口【%s --> %s】",
                key, accumulator.getMaxValue(), accumulator.getMinValue(), accumulator.getAvgValue(), windowStartTime, windowEndTime);

        // 采集器采集结果
        out.collect(result);
    }
}