package seu.virtualcampus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import seu.virtualcampus.domain.OrderItem;
import seu.virtualcampus.mapper.OrderItemMapper;

import java.util.List;

/**
 * 订单项服务类。
 * <p>
 * 提供订单项的增删改查、批量操作等相关业务逻辑。
 * </p>
 */
@Service
public class OrderItemService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    /**
     * 添加订单项。
     *
     * @param orderItem 订单项对象。
     * @return 操作影响的行数。
     */
    public int addOrderItem(OrderItem orderItem) {
        return orderItemMapper.insert(orderItem);
    }

    /**
     * 批量添加订单项。
     *
     * @param orderItems 订单项对象列表。
     * @return 操作影响的行数。
     */
    public int addOrderItemsBatch(List<OrderItem> orderItems) {
        return orderItemMapper.insertBatch(orderItems);
    }

    /**
     * 移除指定订单项。
     *
     * @param itemId 订单项ID。
     * @return 操作影响的行数。
     */
    public int removeOrderItem(String itemId) {
        return orderItemMapper.deleteById(itemId);
    }

    /**
     * 根据订单ID批量移除订单项。
     *
     * @param orderId 订单ID。
     * @return 操作影响的行数。
     */
    public int removeOrderItemsByOrderId(String orderId) {
        return orderItemMapper.deleteByOrderId(orderId);
    }

    /**
     * 更新订单项。
     *
     * @param orderItem 订单项对象。
     * @return 操作影响的行数。
     */
    public int updateOrderItem(OrderItem orderItem) {
        return orderItemMapper.update(orderItem);
    }

    /**
     * 根据ID获取订单项。
     *
     * @param itemId 订单项ID。
     * @return 对应的订单项对象，若不存在则返回null。
     */
    public OrderItem getOrderItemById(String itemId) {
        return orderItemMapper.selectById(itemId);
    }

    /**
     * 根据订单ID获取所有订单项。
     *
     * @param orderId 订单ID。
     * @return 该订单下的所有订单项列表。
     */
    public List<OrderItem> getOrderItemsByOrderId(String orderId) {
        return orderItemMapper.selectByOrderId(orderId);
    }

    /**
     * 获取所有订单项。
     *
     * @return 所有订单项列表。
     */
    public List<OrderItem> getAllOrderItems() {
        return orderItemMapper.selectAll();
    }
}