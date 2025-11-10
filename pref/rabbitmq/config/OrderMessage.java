package com.universe.life.rabbitmq.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单消息模型
 * <p>
 * 用于订单相关业务的消息传递，包括：
 * 1. 订单创建通知
 * 2. 订单支付成功
 * 3. 订单发货通知
 * 4. 订单取消
 * 5. 订单完成
 * </p>
 *
 * @author BMad Optimizer
 * @since 2025/11/10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class OrderMessage extends RabbitMqBaseMessage {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 订单状态（0:待支付 1:已支付 2:已发货 3:已完成 4:已取消）
     */
    private Integer orderStatus;

    /**
     * 支付方式（1:微信 2:支付宝 3:银行卡）
     */
    private Integer paymentMethod;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 发货时间
     */
    private LocalDateTime shipTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 收货地址
     */
    private String shippingAddress;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 订单商品列表
     */
    private List<OrderItem> orderItems;

    /**
     * 操作类型（CREATE, PAY, SHIP, CANCEL, COMPLETE）
     */
    private String operationType;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 操作者
     */
    private String operator;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 构造函数
     */
    public OrderMessage() {
        super();
        this.setMessageType("ORDER_MESSAGE");
    }

    /**
     * 构造函数
     *
     * @param orderId      订单ID
     * @param orderNo      订单编号
     * @param operationType 操作类型
     */
    public OrderMessage(Long orderId, String orderNo, String operationType) {
        this();
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.operationType = operationType;
        this.operationTime = LocalDateTime.now();
    }

    /**
     * 创建订单创建消息
     *
     * @param orderId    订单ID
     * @param orderNo    订单编号
     * @param userId     用户ID
     * @param totalAmount 订单总金额
     * @return 订单消息
     */
    public static OrderMessage createOrderMessage(Long orderId, String orderNo, Long userId, BigDecimal totalAmount) {
        return new OrderMessage(orderId, orderNo, "CREATE")
                .setUserId(userId)
                .setTotalAmount(totalAmount)
                .setOrderStatus(0)
                .addExtension("event", "ORDER_CREATE");
    }

    /**
     * 创建订单支付成功消息
     *
     * @param orderId      订单ID
     * @param orderNo      订单编号
     * @param paymentMethod 支付方式
     * @param paymentTime  支付时间
     * @return 订单消息
     */
    public static OrderMessage createPaymentSuccessMessage(Long orderId, String orderNo,
                                                         Integer paymentMethod, LocalDateTime paymentTime) {
        return new OrderMessage(orderId, orderNo, "PAY")
                .setPaymentMethod(paymentMethod)
                .setPaymentTime(paymentTime)
                .setOrderStatus(1)
                .addExtension("event", "ORDER_PAYMENT_SUCCESS");
    }

    /**
     * 创建订单发货消息
     *
     * @param orderId 订单ID
     * @param orderNo 订单编号
     * @param shipTime 发货时间
     * @return 订单消息
     */
    public static OrderMessage createShipMessage(Long orderId, String orderNo, LocalDateTime shipTime) {
        return new OrderMessage(orderId, orderNo, "SHIP")
                .setShipTime(shipTime)
                .setOrderStatus(2)
                .addExtension("event", "ORDER_SHIP");
    }

    /**
     * 创建订单完成消息
     *
     * @param orderId 订单ID
     * @param orderNo 订单编号
     * @return 订单消息
     */
    public static OrderMessage createCompleteMessage(Long orderId, String orderNo) {
        return new OrderMessage(orderId, orderNo, "COMPLETE")
                .setOrderStatus(3)
                .setCompleteTime(LocalDateTime.now())
                .addExtension("event", "ORDER_COMPLETE");
    }

    /**
     * 订单商品项
     */
    @Data
    @Accessors(chain = true)
    public static class OrderItem {
        /**
         * 商品ID
         */
        private Long productId;

        /**
         * 商品名称
         */
        private String productName;

        /**
         * 商品数量
         */
        private Integer quantity;

        /**
         * 商品单价
         */
        private BigDecimal unitPrice;

        /**
         * 商品总价
         */
        private BigDecimal totalPrice;

        /**
         * 商品图片URL
         */
        private String imageUrl;

        /**
         * 商品规格
         */
        private String specifications;
    }
}