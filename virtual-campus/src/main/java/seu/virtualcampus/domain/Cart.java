package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 购物车项目实体类。
 * <p>
 * 代表用户购物车中的一件商品。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    /**
     * 购物车项目的唯一ID。
     */
    private String cartItemId;
    /**
     * 所属用户的ID。
     */
    private String userId;
    /**
     * 购物车中商品的ID。
     */
    private String productId;
    /**
     * 商品的数量。
     */
    private Integer quantity;
    /**
     * 标记该项目是否为活动状态（例如，用于后续结算）。
     * <p>
     * 通常 1 表示活动，0 表示非活动。
     */
    private Integer isActive;
}