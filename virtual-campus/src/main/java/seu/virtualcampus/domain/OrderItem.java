package seu.virtualcampus.domain;

public class OrderItem {
    private String itemId;
    private String orderId;
    private Integer quantity;
    private String productId;
    private Double unitPrice;
    private Double subtotal;

    // 构造方法
    public OrderItem() {}

    public OrderItem(String itemId, String orderId, Integer quantity,
                     String productId, Double unitPrice) {
        this.itemId = itemId;
        this.orderId = orderId;
        this.quantity = quantity;
        this.productId = productId;
        this.unitPrice = unitPrice;
        this.subtotal = quantity * unitPrice;
    }

    // Getter和Setter方法
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        this.subtotal = this.quantity * this.unitPrice;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
        this.subtotal = this.quantity * this.unitPrice;
    }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    @Override
    public String toString() {
        return "OrderItem{" +
                "itemId='" + itemId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", quantity=" + quantity +
                ", productId='" + productId + '\'' +
                ", unitPrice=" + unitPrice +
                ", subtotal=" + subtotal +
                '}';
    }
}