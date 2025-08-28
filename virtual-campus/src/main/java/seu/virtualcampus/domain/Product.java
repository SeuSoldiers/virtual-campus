package seu.virtualcampus.domain;

public class Product {
    private String productId;
    private String productName;
    private Double productPrice;
    private Integer availableCount;
    private String productType;

    // 构造方法
    public Product() {}

    public Product(String productId, String productName, Double productPrice,
                   Integer availableCount, String productType) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.availableCount = availableCount;
        this.productType = productType;
    }

    // Getter和Setter方法
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Double getProductPrice() { return productPrice; }
    public void setProductPrice(Double productPrice) { this.productPrice = productPrice; }

    public Integer getAvailableCount() { return availableCount; }
    public void setAvailableCount(Integer availableCount) { this.availableCount = availableCount; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", productPrice=" + productPrice +
                ", availableCount=" + availableCount +
                ", productType='" + productType + '\'' +
                '}';
    }
}