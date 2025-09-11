package seu.virtualcampus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import seu.virtualcampus.domain.OrderItem;
import seu.virtualcampus.mapper.OrderItemMapper;
import java.util.List;

@Service
public class OrderItemService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    public int addOrderItem(OrderItem orderItem) {
        return orderItemMapper.insert(orderItem);
    }

    public int removeOrderItem(String itemId) {
        return orderItemMapper.deleteById(itemId);
    }

    public int removeOrderItemsByOrderId(String orderId) {
        return orderItemMapper.deleteByOrderId(orderId);
    }

    public int updateOrderItem(OrderItem orderItem) {
        return orderItemMapper.update(orderItem);
    }

    public OrderItem getOrderItemById(String itemId) {
        return orderItemMapper.selectById(itemId);
    }

    public List<OrderItem> getOrderItemsByOrderId(String orderId) {
        return orderItemMapper.selectByOrderId(orderId);
    }

    public List<OrderItem> getAllOrderItems() {
        return orderItemMapper.selectAll();
    }
}