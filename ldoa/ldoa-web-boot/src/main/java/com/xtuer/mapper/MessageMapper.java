package com.xtuer.mapper;

import com.xtuer.bean.Message;
import com.xtuer.bean.Page;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    /**
     * 插入消息
     *
     * @param message 消息
     */
    void insertMessage(Message message);

    /**
     * 标记消息为已读
     */
    void markMessageRead(long messageId);

    /**
     * 查询指定接收者的消息
     *
     * @param receiverId 接收者 ID
     * @param page 分页信息
     * @return 返回消息数组
     */
    List<Message> findMessagesByReceiverId(long receiverId, Page page);

    /**
     * 统计指定接收者的未读消息
     *
     * @param receiverId 接收者 ID
     * @return 返回未读消息的数量
     */
    int countUnreadMessagesByReceiverId(long receiverId);
}
