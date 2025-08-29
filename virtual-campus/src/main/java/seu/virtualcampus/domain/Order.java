package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String orderId;
    private String userId;
    private Double totalAmount;
    private String status;
    private String orderDate;
    private String paymentMethod;
    private String paymentStatus;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}