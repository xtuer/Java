package rocketmq.quickstart;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

public class Consumer {
    public static void main(String[] args) throws Exception {
        // Push 模式，broker 有消息后主动发送给 consumer。还要 Pull 模式。
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("c_g1");
        // consumer.setNamesrvAddr("localhost:9876");
        consumer.setNamesrvAddr("192.168.1.73:9876");
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.subscribe("TopicTest", "*");
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    System.out.println(Thread.currentThread().getName() + ": " + new String(msg.getBody()));
                }

                // 注意: 如果 topic 里只有几条消息，都消费完后，马上结束 consumer 进程，再次启动  consumer，会发现消息还会重复收到。
                // 如果消费后稍等一会再结束重启 consumer，则消息不会重复收到。
                // 如果有比较多的消息 (如 100 条)，马上结束后重启动很可能不会重复收到。
                // 确认消息被消费应该是在 consumer client 端进行了缓存，并不是马上告诉 broker 消息被消费了。
                // 所以: 在业务逻辑中需要使用唯一的事务 ID，判断事务是否已经处理过，因为重复消费是不可避免的。
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
    }
}
