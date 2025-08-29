package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.Cart;
import java.util.List;

@Mapper
public interface CartMapper {

    @Insert("INSERT INTO cart(cartItemId, userId, productId, quantity, isActive) " +
            "VALUES(#{cartItemId}, #{userId}, #{productId}, #{quantity}, #{isActive})")
    int insert(Cart cart);

    @Delete("DELETE FROM cart WHERE cartItemId = #{cartItemId}")
    int deleteById(String cartItemId);

    @Update("UPDATE cart SET userId=#{userId}, productId=#{productId}, quantity=#{quantity}, " +
            "isActive=#{isActive} WHERE cartItemId=#{cartItemId}")
    int update(Cart cart);

    @Select("SELECT * FROM cart WHERE cartItemId = #{cartItemId}")
    Cart selectById(String cartItemId);

    @Select("SELECT * FROM cart WHERE userId = #{userId} AND isActive = 1")
    List<Cart> selectByUserId(String userId);

    @Select("SELECT * FROM cart WHERE userId = #{userId} AND productId = #{productId} AND isActive = 1")
    Cart selectByUserAndProduct(@Param("userId") String userId, @Param("productId") String productId);

    @Update("UPDATE cart SET isActive = 0 WHERE userId = #{userId}")
    int deactivateAllByUserId(String userId);
}