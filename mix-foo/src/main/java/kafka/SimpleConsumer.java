package kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * 消费主题的消息
 * 发送消息:
 * A. bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test-simple
 * B. 运行 SimpleProducer
 */
public class SimpleConsumer {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        // 提示: 设置消费者组，默认 auto.offset.reset = latest (earliest 更合理), enable.auto.commit = true
        // latest:   组没有消费过，则从消费者启动后产生的消息开始消费，否则从最后消费的下一条消息开始消费
        // earliest: 组没有消费过，则从第一个消息开始消费，否则从最后消费的下一条消息开始消费
        props.put("auto.offset.reset", "earliest");
        props.put("group.id", "Ad-hoc-1");

        // [1] 创建消费者，订阅主题
        Consumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("test-simple"));

        try {
            while (true) {
                // [2] 轮询拉取消息
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                // [3] 处理消息
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("topic = %s, partition = %s, offset = %d, key = %s, value = %s\n",
                            record.topic(), record.partition(), record.offset(), record.key(), record.value());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            // [4] 关闭消费者，触发 rebalance
            consumer.close();
        }
    }
}
