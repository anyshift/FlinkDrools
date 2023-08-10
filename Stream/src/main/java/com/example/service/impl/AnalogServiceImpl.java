package com.example.service.impl;

import cn.hutool.json.JSONUtil;
import com.example.vo.AnalogData;
import com.example.function.analogs.AnalogAggregateFunction;
import com.example.function.analogs.AnalogWindowProcessFunction;
import com.example.service.AnalogService;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.springframework.stereotype.Service;

@Service
public class AnalogServiceImpl implements AnalogService {

    private KafkaSource<String> kafkaSource() {
        String KAFKA_BROKER = "localhost:9092";
        String ANALOG_TOPIC = "analog_data_topic";
        return KafkaSource.<String>builder()
                .setBootstrapServers(KAFKA_BROKER)
                .setGroupId("flink_consumer_group")
                .setTopics(ANALOG_TOPIC)
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
    }

    @Override
    public void handleAlarm() throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<String> fromKafkaSource = env.fromSource(this.kafkaSource(), WatermarkStrategy.noWatermarks(), "Kafka Source");

        SingleOutputStreamOperator<String> aggregateStream = fromKafkaSource
                // 转换数据流
                .map(new ConvertAnalogDataFunction())
                // 按键分区
                .keyBy(AnalogData::getSensorId)
                // 滚动窗口
                .window(TumblingProcessingTimeWindows.of(Time.seconds(5)))
                // 聚合并进行窗口处理（第二个参数会接收第一个参数中处理完毕后的窗口累加器数据）
                .aggregate(AnalogAggregateFunction.getInstance(), AnalogWindowProcessFunction.getInstance());

        aggregateStream.print();

        env.execute("Flink Analog Data Processor");
    }

    /**
     * 将 JSON 数据流依次转换成模拟量对象
     */
    private static class ConvertAnalogDataFunction implements MapFunction<String, AnalogData> {
        @Override
        public AnalogData map(String value) {
            return JSONUtil.toBean(value, AnalogData.class);
        }
    }
}
