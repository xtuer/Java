package com.xtuer.controller;

import com.xtuer.bean.Message;
import com.xtuer.bean.Page;
import com.xtuer.bean.Result;
import com.xtuer.bean.Urls;
import com.xtuer.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 消息的控制器
 */
@RestController
public class MessageController extends BaseController {
    @Autowired
    private MessageMapper messageMapper;

    /**
     * 查询指定接收者的消息
     *
     * 网址: http://localhost:8080/api/messages/receivers/{receiverId}/messages
     * 参数:
     *      pageNumber [可选]: 页码
     *      pageSize   [可选]: 数量
     *
     * @param receiverId 接收者 ID
     * @param page 分页对象
     * @return payload 为消息数组
     */
    @GetMapping(Urls.API_MESSAGES_OF_RECEIVER)
    public Result<List<Message>> findMessagesByReceiverId(@PathVariable long receiverId, Page page) {
        return Result.ok(messageMapper.findMessagesByReceiverId(receiverId, page));
    }

    /**
     * 统计指定接收者的未读消息
     *
     * 网址: http://localhost:8080/api/messages/receivers/{receiverId}/messages/unreadCount
     * 参数: 无
     *
     * @param receiverId 接收者 ID
     * @return payload 为未读消息数量
     */
    @GetMapping(Urls.API_MESSAGES_UNREAD_COUNT_OF_RECEIVER)
    public Result<Integer> countUnreadMessagesByReceiverId(@PathVariable long receiverId) {
        return Result.ok(messageMapper.countUnreadMessagesByReceiverId(receiverId));
    }

    /**
     * 标记消息为已读
     *
     * 网址: http://localhost:8080/api/messages/{messageId}/read
     * 参数: 无
     *
     * @param messageId 消息 ID
     * @return payload 无
     */
    @PutMapping(Urls.API_MESSAGES_AS_READ)
    public Result<Boolean> markMessageRead(@PathVariable long messageId) {
        messageMapper.markMessageRead(messageId);
        return Result.ok();
    }
}
