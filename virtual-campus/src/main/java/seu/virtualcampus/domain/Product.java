package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品实体类。
 * <p>
 * 代表商店中可供销售的商品信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    /**
     * 商品的唯一ID。
     */
    private String productId;
    /**
     * 商品的名称。
     */
    private String productName;
    /**
     * 商品的单价。
     */
    private Double productPrice;
    /**
     * 商品的可用库存数量。
     */
    private Integer availableCount;
    /**
     * 商品的类型或分类。
     */
    private String productType;
    /**
     * 商品的状态。
     * <p>
     * 例如: "ACTIVE" (上架), "INACTIVE" (下架)。
     */
    private String status; // 商品状态：ACTIVE(上架)、INACTIVE(下架)
}