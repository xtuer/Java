package com.xtuer.service;

import com.xtuer.bean.Message;
import com.xtuer.bean.order.Order;
import com.xtuer.mapper.MessageMapper;
import com.xtuer.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 消息服务
 */
@Service
@Slf4j
public class MessageService extends BaseService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageMapper messageMapper;

    /**
     * 发送订单消息
     *
     * @param type  消息类型
     * @param order 订单
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void sendOrderMessage(Message.Type type, Order order) {
        // 生成订单创建或者编辑后发送消息给生产部质量保证、生产部计划调度、销售部综合保证
        List<String> roles = Arrays.asList("ROLE_PRODUCE_QUALITY", "ROLE_PRODUCE_SCHEDULE", "ROLE_SALE_GUARANTEE");
        List<Long> userIds = userMapper.findUserIdsByRoles(roles);

        if (userIds.size() == 0) {
            log.info("[注意] 生产部质量保证、生产部计划调度、销售部综合保证没有用户");
            return;
        }

        // 发送消息
        log.info("[消息-开始] 发送订单消息");

        for (Long receiverId : userIds) {
            log.info("[消息-发送] 发送订单消息, 接收者 [{}], 类型 [{}], 订单 [{}]", receiverId, type, order.getOrderId());
            String content = String.format("%s, 订单号 %s", type.getLabel(), order.getOrderSn());
            Message message = new Message(super.nextId(), 0L, receiverId, order.getOrderId(), type, content);
            messageMapper.insertMessage(message);
        }

        log.info("[消息-结束] 发送订单消息");
    }
}
