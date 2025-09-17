package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.Cart;
import java.util.List;

/**
 * 购物车Mapper接口。
 * <p>
 * 定义了与数据库中cart表相关的操作。
 */
@Mapper
public interface CartMapper {

    /**
     * 向购物车中插入一个新项目。
     *
     * @param cart 要插入的购物车项目对象。
     * @return 受影响的行数。
     */
    @Insert("INSERT INTO cart(cartItemId, userId, productId, quantity, isActive) " +
            "VALUES(#{cartItemId}, #{userId}, #{productId}, #{quantity}, #{isActive})")
    int insert(Cart cart);

    /**
     * 根据ID从购物车中删除一个项目。
     *
     * @param cartItemId 要删除的购物车项目ID。
     * @return 受影响的行数。
     */
    @Delete("DELETE FROM cart WHERE cartItemId = #{cartItemId}")
    int deleteById(String cartItemId);

    /**
     * 更新一个购物车项目的信息。
     *
     * @param cart 包含更新信息的购物车项目对象。
     * @return 受影响的行数。
     */
    @Update("UPDATE cart SET userId=#{userId}, productId=#{productId}, quantity=#{quantity}, " +
            "isActive=#{isActive} WHERE cartItemId=#{cartItemId}")
    int update(Cart cart);

    /**
     * 根据ID查询一个购物车项目。
     *
     * @param cartItemId 购物车项目ID。
     * @return 对应的购物车项目对象，如果不存在则返回null。
     */
    @Select("SELECT * FROM cart WHERE cartItemId = #{cartItemId}")
    Cart selectById(String cartItemId);

    /**
     * 查询一个用户购物车中所有活动的商品。
     *
     * @param userId 用户ID。
     * @return 该用户购物车中所有活动项目的列表。
     */
    @Select("SELECT * FROM cart WHERE userId = #{userId} AND isActive = 1")
    List<Cart> selectByUserId(String userId);

    /**
     * 查询用户购物车中是否已存在某个活动状态的商品。
     *
     * @param userId    用户ID。
     * @param productId 商品ID。
     * @return 对应的购物车项目对象，如果不存在则返回null。
     */
    @Select("SELECT * FROM cart WHERE userId = #{userId} AND productId = #{productId} AND isActive = 1")
    Cart selectByUserAndProduct(@Param("userId") String userId, @Param("productId") String productId);

    /**
     * 将用户购物车中的所有项目设置为非活动状态。
     *
     * @param userId 用户ID。
     * @return 受影响的行数。
     */
    @Update("UPDATE cart SET isActive = 0 WHERE userId = #{userId}")
    int deactivateAllByUserId(String userId);

    /**
     * 更新购物车中某个项目的数量。
     *
     * @param cartItemId 购物车项目ID。
     * @param quantity   新的数量。
     * @return 受影响的行数。
     */
    @Update("UPDATE cart SET quantity = #{quantity} WHERE cartItemId = #{cartItemId}")
    int updateQuantity(@Param("cartItemId") String cartItemId, @Param("quantity") Integer quantity);

    /**
     * 根据ID列表批量查询购物车项目。
     * (此方法的SQL实现在XML映射文件中定义)
     *
     * @param cartItemIds 购物车项目ID的列表。
     * @return 匹配的购物车项目列表。
     */
    List<Cart> selectByIds(List<String> cartItemIds);

    /**
     * 根据ID列表批量删除购物车项目。
     * (此方法的SQL实现在XML映射文件中定义)
     *
     * @param cartItemIds 要删除的购物车项目ID的列表。
     * @return 受影响的行数。
     */
    int deleteByIds(List<String> cartItemIds);
}