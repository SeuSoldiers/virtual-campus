package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单项实体类。
 * <p>
 * 代表一个订单中包含的单项商品及其数量和价格信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    /**
     * 订单项的唯一ID。
     */
    private String itemId;
    /**
     * 所属订单的ID。
     */
    private String orderId;
    /**
     * 本订单项中商品的数量。
     */
    private Integer quantity;
    /**
     * 商品的ID。
     */
    private String productId;
    /**
     * 商品的单价。
     */
    private Double unitPrice;
    /**
     * 本订单项的小计金额 (quantity * unitPrice)。
     */
    private Double subtotal;
}