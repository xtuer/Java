package rocketmq.quickstart;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;

import java.nio.charset.StandardCharsets;

public class Producer {
    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("p_g1"); // 需要指定 producer group
        producer.setNamesrvAddr("localhost:9876"); // 如果设置了环境变量 NAMESRV_ADDR 可以不设置 name server
        producer.start();

        for (int i = 1; i <= 10; i++) {
            Message msg = new Message("t3", ("MSG6 " + i).getBytes(StandardCharsets.UTF_8));
            producer.send(msg);
        }

        producer.shutdown();
    }
}
