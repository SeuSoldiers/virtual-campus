package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String productId;
    private String productName;
    private Double productPrice;
    private Integer availableCount;
    private String productType;
    private String status; // 商品状态：ACTIVE(上架)、INACTIVE(下架)
}