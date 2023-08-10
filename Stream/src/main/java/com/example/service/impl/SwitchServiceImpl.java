package com.example.service.impl;

import cn.hutool.json.JSONUtil;
import com.example.vo.SwitchData;
import com.example.function.switchs.SwitchAggregateFunction;
import com.example.function.switchs.SwitchWindowProcessFunction;
import com.example.service.SwitchService;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.springframework.stereotype.Service;

@Service
public class SwitchServiceImpl implements SwitchService {

    private KafkaSource<String> kafkaSource() {
        String KAFKA_BROKER = "localhost:9092";
        String SWITCH_TOPIC = "switch_data_topic";
        return KafkaSource.<String>builder()
                .setBootstrapServers(KAFKA_BROKER)
                .setGroupId("flink_consumer_group")
                .setTopics(SWITCH_TOPIC)
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
    }

    @Override
    public void handleAlarm() throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<String> aggregateStream = env.fromSource(this.kafkaSource(), WatermarkStrategy.noWatermarks(), "Kafka Source")
                // 转换数据流
                .map(new SwitchServiceImpl.ConvertSwitchDataFunction())
                // 按键分区
                .keyBy(SwitchData::getDeviceId)
                // 滚动窗口
                .window(TumblingProcessingTimeWindows.of(Time.seconds(5)))
                // 聚合并进行窗口处理（第二个参数会接收第一个参数中处理完毕后的窗口累加器数据）
                .aggregate(SwitchAggregateFunction.getInstance(), SwitchWindowProcessFunction.getInstance());

        aggregateStream.print();

        env.execute("Flink Switch Data Processor");
    }

    /**
     * 将 JSON 数据流依次转换成开关量对象
     */
    private static class ConvertSwitchDataFunction implements MapFunction<String, SwitchData> {
        @Override
        public SwitchData map(String value) {
            return JSONUtil.toBean(value, SwitchData.class);
        }
    }
}
