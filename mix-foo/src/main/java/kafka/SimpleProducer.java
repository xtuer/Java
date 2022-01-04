package kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 发送: 简单的发送消息到 Kafka
 * 查看: bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test-simple --from-beginning
 */
public class SimpleProducer {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        ProducerRecord<String, String> record = new ProducerRecord<>("test-simple", "alice", "Message-" + System.currentTimeMillis());
        Future<RecordMetadata> future = producer.send(record);

        // 注意: 演示程序中要同步等待发送结束，否则 main 线程立即结束导致发送线程结束，以至于消息没有发送
        RecordMetadata result = future.get();
        System.out.println(result);
    }
}
