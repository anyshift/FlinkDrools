package com.example;

import cn.hutool.json.JSONUtil;
import com.example.service.AnalogService;
import com.example.service.SwitchService;
import com.example.vo.AnalogData;
import com.example.vo.SwitchData;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@SpringBootTest
class KafkaApplicationTests {

    @Resource
    private AnalogService analogService;

    @Resource
    private SwitchService switchService;

    private static final String KAFKA_BROKER = "localhost:9092";
    private static final String ANALOG_TOPIC = "analog_data_topic";
    private static final String SWITCH_TOPIC = "switch_data_topic";

    // 消费者(模拟量)
    @Test
    void analogDataConsumer() throws Exception {
        analogService.handleAlarm();
    }

    // 消费者(开关量)
    @Test
    void switchDataConsumer() throws Exception {
        switchService.handleAlarm();
    }

    // Kafka 生产者
    @Test
    void kafkaProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", KAFKA_BROKER);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        List<String> analogNames = new ArrayList<>();
        analogNames.add("温度传感器");
        analogNames.add("水位传感器");
        analogNames.add("压力传感器");
        analogNames.add("声音传感器");
        analogNames.add("烟雾传感器");

        List<String> switchNames = new ArrayList<>();
        switchNames.add("灯泡控制器");
        switchNames.add("水泵控制器");
        switchNames.add("发电控制器");
        switchNames.add("电量检测仪");
        switchNames.add("电机断路器");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 1; i <= 500; i++) {
                String topic;
                String data;

                double random = Math.random();
                int randomFive = (int) (Math.random() * 5);
                String analogName = analogNames.get(randomFive) + "-" + randomFive;
                String switchName = switchNames.get(randomFive) + "-" + randomFive;

                if (random > 0.5) {
                    Double value = Math.random() * 100;
                    Long timestamp = System.currentTimeMillis();

                    AnalogData analogData = new AnalogData(analogName, value, timestamp);
                    data = JSONUtil.toJsonStr(analogData);
                    topic = ANALOG_TOPIC;
                } else {
                    Boolean status = Math.random() > 0.5;
                    Long timestamp = System.currentTimeMillis();

                    SwitchData switchData = new SwitchData(switchName, status, timestamp);
                    data = JSONUtil.toJsonStr(switchData);
                    topic = SWITCH_TOPIC;
                }

                ProducerRecord<String, String> record = new ProducerRecord<>(topic, data);
                producer.send(record);

                System.out.println("剩余" + (500 - i) + "秒：" + data);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
