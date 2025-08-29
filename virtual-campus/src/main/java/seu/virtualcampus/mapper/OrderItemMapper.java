package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.OrderItem;
import java.util.List;

@Mapper
public interface OrderItemMapper {

    @Insert("INSERT INTO order_item(itemId, orderId, quantity, productId, unitPrice, subtotal) " +
            "VALUES(#{itemId}, #{orderId}, #{quantity}, #{productId}, #{unitPrice}, #{subtotal})")
    int insert(OrderItem orderItem);

    @Delete("DELETE FROM order_item WHERE itemId = #{itemId}")
    int deleteById(String itemId);

    @Delete("DELETE FROM order_item WHERE orderId = #{orderId}")
    int deleteByOrderId(String orderId);

    @Update("UPDATE order_item SET orderId=#{orderId}, quantity=#{quantity}, productId=#{productId}, " +
            "unitPrice=#{unitPrice}, subtotal=#{subtotal} WHERE itemId=#{itemId}")
    int update(OrderItem orderItem);

    @Select("SELECT * FROM order_item WHERE itemId = #{itemId}")
    OrderItem selectById(String itemId);

    @Select("SELECT * FROM order_item WHERE orderId = #{orderId}")
    List<OrderItem> selectByOrderId(String orderId);

    @Select("SELECT * FROM order_item")
    List<OrderItem> selectAll();
}