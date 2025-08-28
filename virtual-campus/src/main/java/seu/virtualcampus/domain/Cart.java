package seu.virtualcampus.domain;

public class Cart {
    private String cartItemId;
    private String userId;
    private String productId;
    private Integer quantity;
    private Integer isActive;

    // 构造方法
    public Cart() {}

    public Cart(String cartItemId, String userId, String productId, Integer quantity) {
        this.cartItemId = cartItemId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.isActive = 1; // 默认激活状态
    }

    // Getter和Setter方法
    public String getCartItemId() { return cartItemId; }
    public void setCartItemId(String cartItemId) { this.cartItemId = cartItemId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getIsActive() { return isActive; }
    public void setIsActive(Integer isActive) { this.isActive = isActive; }

    @Override
    public String toString() {
        return "Cart{" +
                "cartItemId='" + cartItemId + '\'' +
                ", userId='" + userId + '\'' +
                ", productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", isActive=" + isActive +
                '}';
    }
}