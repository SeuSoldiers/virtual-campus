package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.OrderItem;
import java.util.List;

/**
 * 订单项Mapper接口。
 * <p>
 * 定义了与数据库中order_item表相关的操作。
 */
@Mapper
public interface OrderItemMapper {

    /**
     * 插入一个订单项。
     *
     * @param orderItem 要插入的订单项对象。
     * @return 受影响的行数。
     */
    @Insert("INSERT INTO order_item(itemId, orderId, quantity, productId, unitPrice, subtotal) " +
            "VALUES(#{itemId}, #{orderId}, #{quantity}, #{productId}, #{unitPrice}, #{subtotal})")
    int insert(OrderItem orderItem);

    /**
     * 根据ID删除一个订单项。
     *
     * @param itemId 要删除的订单项ID。
     * @return 受影响的行数。
     */
    @Delete("DELETE FROM order_item WHERE itemId = #{itemId}")
    int deleteById(String itemId);

    /**
     * 根据订单ID删除该订单下的所有订单项。
     *
     * @param orderId 订单ID。
     * @return 受影响的行数。
     */
    @Delete("DELETE FROM order_item WHERE orderId = #{orderId}")
    int deleteByOrderId(String orderId);

    /**
     * 更新一个订单项的信息。
     *
     * @param orderItem 包含更新信息的订单项对象。
     * @return 受影响的行数。
     */
    @Update("UPDATE order_item SET orderId=#{orderId}, quantity=#{quantity}, productId=#{productId}, " +
            "unitPrice=#{unitPrice}, subtotal=#{subtotal} WHERE itemId=#{itemId}")
    int update(OrderItem orderItem);

    /**
     * 根据ID查询一个订单项。
     *
     * @param itemId 订单项ID。
     * @return 对应的订单项对象，如果不存在则返回null。
     */
    @Select("SELECT * FROM order_item WHERE itemId = #{itemId}")
    OrderItem selectById(String itemId);

    /**
     * 根据订单ID查询其下所有的订单项。
     *
     * @param orderId 订单ID。
     * @return 该订单的所有订单项列表。
     */
    @Select("SELECT * FROM order_item WHERE orderId = #{orderId}")
    List<OrderItem> selectByOrderId(String orderId);

    /**
     * 查询所有的订单项。
     *
     * @return 数据库中所有订单项的列表。
     */
    @Select("SELECT * FROM order_item")
    List<OrderItem> selectAll();

    /**
     * 批量插入订单项。
     * (此方法的SQL实现在XML映射文件中定义)
     *
     * @param orderItems 要批量插入的订单项列表。
     * @return 受影响的行数。
     */
    int insertBatch(List<OrderItem> orderItems);
}