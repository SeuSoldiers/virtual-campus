package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单实体类。
 * <p>
 * 代表一个用户的购物订单信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    /**
     * 订单的唯一ID。
     */
    private String orderId;
    /**
     * 下单用户的ID。
     */
    private String userId;
    /**
     * 订单的总金额。
     */
    private Double totalAmount;
    /**
     * 订单的当前状态。
     * <p>
     * 例如: "PENDING" (待支付), "PAID" (已支付), "DELIVERED" (已发货), "CONFIRMED" (已收货), "CANCELLED" (已取消)。
     */
    private String status;
    /**
     * 下单日期，格式通常为字符串。
     */
    private String orderDate;
    /**
     * 支付方式。
     */
    private String paymentMethod;
    /**
     * 支付状态。
     */
    private String paymentStatus;
    /**
     * 订单记录的创建时间。
     */
    private java.time.LocalDateTime createdAt;
    /**
     * 订单记录的最后更新时间。
     */
    private java.time.LocalDateTime updatedAt;
}