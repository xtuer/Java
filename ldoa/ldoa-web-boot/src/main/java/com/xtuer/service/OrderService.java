package com.xtuer.service;

import com.xtuer.bean.*;
import com.xtuer.bean.audit.Audit;
import com.xtuer.bean.order.Order;
import com.xtuer.bean.order.OrderFilter;
import com.xtuer.bean.order.OrderItem;
import com.xtuer.bean.product.Product;
import com.xtuer.mapper.FileMapper;
import com.xtuer.mapper.OrderMapper;
import com.xtuer.mapper.ProductMapper;
import com.xtuer.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 订单服务
 */
@Service
public class OrderService extends BaseService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private AuditServiceHelper auditServiceHelper;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private MessageService messageService;

    /**
     * 查询指定 ID 的订单
     *
     * @param orderId 订单 ID
     * @return 返回查询到的订单
     */
    public Order findOrder(long orderId) {
        // 1. 查询订单的基础数据 (包含了订单项，销售员，产品的基本信息 (不包含产品项) 等)
        // 2. 去掉 orderItemId 为 0 的 item (没有订单项时 MyBatis 的绑定造成多出一个无效的订单项)
        // 3. 查询订单项的产品
        // 4. 查询订单的附件
        Order order = orderMapper.findOrderById(orderId);

        if (order == null) {
            return null;
        }

        // [2] 去掉 orderItemId 为 0 的 item (没有订单项时 MyBatis 的绑定造成多出一个无效的订单项)
        List<OrderItem> validOrderItems = order.getItems().stream().filter(item -> item.getOrderItemId() > 0).collect(Collectors.toList());
        order.setItems(validOrderItems);

        // [3] 查询订单项的产品
        order.getItems().forEach(orderItem -> {
            Product product = productMapper.findProductById(orderItem.getProductId());
            orderItem.setProduct(product);
        });

        // [4] 查询订单的附件
        UploadedFile attachment = fileMapper.findUploadedFileById(order.getAttachmentId());
        if (attachment != null) {
            order.setAttachment(attachment);
        }

        return order;
    }

    /**
     * 插入或者更新订单
     *
     * @param order 订单
     * @param salesperson 销售员
     * @return 返回操作的结果，payload 为更新后的订单
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Order> upsertOrder(Order order, User salesperson) {
        // 1. 如果订单 ID 无效，则说明是创建，分配订单 ID、订单号和销售员
        // 2. 设置订单的产品编码
        // 3. 设置订单项
        // 4. 创建订单的审批，失败则不继续处理
        // 5. 删除订单的订单项
        // 6. 重新创建订单项
        // 7. 保存附件
        // 8. 保存订单到数据库
        // 9. 发送订单消息

        // 订单的消息类型，默认为更新订单
        Message.Type messageType = Message.Type.UPDATE_ORDER;

        // [1] 如果订单 ID 无效，则说明是创建，分配订单 ID 和订单号
        if (Utils.isInvalidId(order.getOrderId())) {
            order.setOrderId(super.nextId());
            order.setOrderSn(nextOrderSn());
            order.setSalespersonId(salesperson.getUserId());
            order.setSalesperson(salesperson);
            messageType = Message.Type.CREATE_ORDER;
        }
        order.setState(Order.STATE_AUDITING); // 状态为等待审批

        // [2] 设置订单的产品名称
        order.setProductCodes(this.getOrderProductCodes(order));
        order.setProductNames(this.getOrderProductNames(order));


        // [3] 设置订单项
        for (OrderItem item : order.getItems()) {
            item.setOrderId(order.getOrderId());

            if (item.getProduct() != null) {
                item.setProductId(item.getProduct().getProductId());
            }

            if (Utils.isInvalidId(item.getOrderItemId())) {
                item.setOrderItemId(super.nextId());
            }
        }

        // [4] 创建订单的审批，失败则不继续处理
        Result<Audit> result = auditServiceHelper.upsertOrderAudit(salesperson, order);
        if (!result.isSuccess()) {
            return Result.fail(result);
        }

        // [5] 删除订单的订单项
        orderMapper.deleteOrderItems(order.getOrderId());

        // [6] 重新创建订单项
        if (!order.getItems().isEmpty()) {
            orderMapper.insertOrderItems(order.getItems());
        }

        // [7] 保存附件
        repoFileService.moveTempFileToRepo(order.getAttachmentId());

        // [8] 保存订单到数据库
        orderMapper.upsertOrder(order);
        orderMapper.updateOrderState(order.getOrderId(), order.getState());

        // [9] 发送订单消息
        messageService.sendOrderMessage(messageType, order);

        return Result.ok(order);
    }

    /**
     * 获取订单的所有产品编码
     *
     * @param order 订单
     * @return 返回产品编码
     */
    public String getOrderProductCodes(Order order) {
        return order.getItems()                    // 获取订单项
                .stream()                          // 转为 stream
                .map(OrderItem::getProduct)        // 获取订单项的产品
                .filter(Objects::nonNull)          // 去掉 null 的产品
                .map(Product::getCode)             // 获取产品的编码
                .collect(Collectors.joining(",")); // 返回逗号分隔的产品编码
    }

    /**
     * 获取订单的所有产品名称
     *
     * @param order 订单
     * @return 返回产品名称
     */
    public String getOrderProductNames(Order order) {
        return order.getItems()                    // 获取订单项
                .stream()                          // 转为 stream
                .map(OrderItem::getProduct)        // 获取订单项的产品
                .filter(Objects::nonNull)          // 去掉 null 的产品
                .map(Product::getName)             // 获取产品的名称
                .collect(Collectors.joining(",")); // 返回逗号分隔的产品编码
    }

    /**
     * 生成订单号，格式为 XSDD-20200806-0001
     *
     * @return 返回订单号
     */
    public String nextOrderSn() {
        // XSDD 不动, 20200806 为年月日，根据日期自动生成, 0001 为流水号 (0001,0002,003……)，每年再从 0001 开始
        return super.nextSnByYear(Const.SN_PREFIX_ORDER);
    }

    /**
     * 订单审批通过
     *
     * @param orderId 订单 ID
     */
    public void acceptOrder(long orderId) {
        orderMapper.updateOrderState(orderId, Order.STATE_ACCEPTED);
    }

    /**
     * 订单审批被拒绝
     *
     * @param orderId 订单 ID
     */
    public void rejectOrder(long orderId) {
        orderMapper.updateOrderState(orderId, Order.STATE_REJECTED);
    }

    /**
     * 完成订单
     *
     * @param orderId 订单 ID
     */
    public void completeOrder(long orderId) {
        orderMapper.updateOrderState(orderId, Order.STATE_COMPLETE);
    }

    /**
     * 导出生产订单
     *
     * @param filter 过滤条件
     * @return 返回导出的 Excel 的 URL
     */
    public String exportOrders(OrderFilter filter) throws IOException {
        Page page = Page.of(1, Integer.MAX_VALUE);
        List<Order> orders = orderMapper.findOrders(filter, page);
        return super.exportExcel("生产订单", Order.class, orders);
    }
}
