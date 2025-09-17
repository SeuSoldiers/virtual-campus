package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.Order;
import java.util.List;

/**
 * 订单Mapper接口。
 * <p>
 * 定义了与数据库中orders表相关的操作。
 */
@Mapper
public interface OrderMapper {

    /**
     * 插入一个新订单。
     *
     * @param order 要插入的订单对象。
     * @return 受影响的行数。
     */
    @Insert("INSERT INTO orders(orderId, userId, totalAmount, status, orderDate, " +
            "paymentMethod, paymentStatus, createdAt, updatedAt) " +
            "VALUES(#{orderId}, #{userId}, #{totalAmount}, #{status}, #{orderDate}, " +
            "#{paymentMethod}, #{paymentStatus}, #{createdAt}, #{updatedAt})")
    int insert(Order order);

    /**
     * 根据ID删除一个订单。
     *
     * @param orderId 要删除的订单ID。
     * @return 受影响的行数。
     */
    @Delete("DELETE FROM orders WHERE orderId = #{orderId}")
    int deleteById(String orderId);

    /**
     * 更新一个订单的完整信息。
     *
     * @param order 包含更新信息的订单对象。
     * @return 受影响的行数。
     */
    @Update("UPDATE orders SET userId=#{userId}, totalAmount=#{totalAmount}, status=#{status}, " +
            "orderDate=#{orderDate}, paymentMethod=#{paymentMethod}, paymentStatus=#{paymentStatus}, " +
            "updatedAt=#{updatedAt} WHERE orderId=#{orderId}")
    int update(Order order);

    /**
     * 根据ID查询一个订单。
     *
     * @param orderId 订单ID。
     * @return 对应的订单对象，如果不存在则返回null。
     */
    @Select("SELECT * FROM orders WHERE orderId = #{orderId}")
    Order selectById(String orderId);

    /**
     * 根据用户ID查询其所有订单。
     *
     * @param userId 用户ID。
     * @return 该用户的所有订单列表。
     */
    @Select("SELECT * FROM orders WHERE userId = #{userId}")
    List<Order> selectByUserId(String userId);

    /**
     * 查询所有的订单。
     *
     * @return 数据库中所有订单的列表。
     */
    @Select("SELECT * FROM orders")
    List<Order> selectAll();

    /**
     * 更新订单的状态和支付状态。
     *
     * @param orderId       订单ID。
     * @param status        新的订单状态。
     * @param paymentStatus 新的支付状态。
     * @return 受影响的行数。
     */
    @Update("UPDATE orders SET status=#{status}, paymentStatus=#{paymentStatus}, updatedAt=#{updatedAt} " +
            "WHERE orderId=#{orderId}")
    int updateStatus(@Param("orderId") String orderId, @Param("status") String status,
                     @Param("paymentStatus") String paymentStatus);
}