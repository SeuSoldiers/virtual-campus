package seu.virtualcampus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import seu.virtualcampus.domain.Order;
import seu.virtualcampus.mapper.OrderMapper;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    public int createOrder(Order order) {
        return orderMapper.insert(order);
    }

    public int cancelOrder(String orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order != null) {
            order.setStatus("CANCELLED");
            order.setUpdatedAt(LocalDateTime.now());
            return orderMapper.update(order);
        }
        return 0;
    }

    public int updateOrder(Order order) {
        order.setUpdatedAt(LocalDateTime.now());
        return orderMapper.update(order);
    }

    public Order getOrderById(String orderId) {
        return orderMapper.selectById(orderId);
    }

    public List<Order> getOrdersByUserId(String userId) {
        return orderMapper.selectByUserId(userId);
    }

    public List<Order> getAllOrders() {
        return orderMapper.selectAll();
    }

    public int updateOrderStatus(String orderId, String status, String paymentStatus) {
        return orderMapper.updateStatus(orderId, status, paymentStatus);
    }
}