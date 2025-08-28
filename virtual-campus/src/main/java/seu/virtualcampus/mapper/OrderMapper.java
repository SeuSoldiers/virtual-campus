package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.Order;
import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO orders(orderId, userId, totalAmount, status, orderDate, " +
            "paymentMethod, paymentStatus, createdAt, updatedAt) " +
            "VALUES(#{orderId}, #{userId}, #{totalAmount}, #{status}, #{orderDate}, " +
            "#{paymentMethod}, #{paymentStatus}, #{createdAt}, #{updatedAt})")
    int insert(Order order);

    @Delete("DELETE FROM orders WHERE orderId = #{orderId}")
    int deleteById(String orderId);

    @Update("UPDATE orders SET userId=#{userId}, totalAmount=#{totalAmount}, status=#{status}, " +
            "orderDate=#{orderDate}, paymentMethod=#{paymentMethod}, paymentStatus=#{paymentStatus}, " +
            "updatedAt=#{updatedAt} WHERE orderId=#{orderId}")
    int update(Order order);

    @Select("SELECT * FROM orders WHERE orderId = #{orderId}")
    Order selectById(String orderId);

    @Select("SELECT * FROM orders WHERE userId = #{userId}")
    List<Order> selectByUserId(String userId);

    @Select("SELECT * FROM orders")
    List<Order> selectAll();

    @Update("UPDATE orders SET status=#{status}, paymentStatus=#{paymentStatus}, updatedAt=#{updatedAt} " +
            "WHERE orderId=#{orderId}")
    int updateStatus(@Param("orderId") String orderId, @Param("status") String status,
                     @Param("paymentStatus") String paymentStatus);
}