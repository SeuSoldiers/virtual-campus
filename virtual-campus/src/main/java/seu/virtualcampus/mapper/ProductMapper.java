package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.Product;
import java.util.List;

@Mapper
public interface ProductMapper {

    @Insert("INSERT INTO product(productId, productName, productPrice, availableCount, productType) " +
            "VALUES(#{productId}, #{productName}, #{productPrice}, #{availableCount}, #{productType})")
    int insert(Product product);

    @Delete("DELETE FROM product WHERE productId = #{productId}")
    int deleteById(String productId);

    @Update("UPDATE product SET productName=#{productName}, productPrice=#{productPrice}, " +
            "availableCount=#{availableCount}, productType=#{productType} WHERE productId=#{productId}")
    int update(Product product);

    @Select("SELECT * FROM product WHERE productId = #{productId}")
    Product selectById(String productId);

    @Select("SELECT * FROM product")
    List<Product> selectAll();

    @Select("SELECT * FROM product WHERE productType = #{productType}")
    List<Product> selectByType(String productType);

    @Update("UPDATE product SET availableCount = availableCount - #{quantity} " +
            "WHERE productId = #{productId} AND availableCount >= #{quantity}")
    int reduceStock(@Param("productId") String productId, @Param("quantity") Integer quantity);
}