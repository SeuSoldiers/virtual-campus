package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.Product;
import java.util.List;

@Mapper
public interface ProductMapper {

    @Insert("INSERT INTO product(productId, productName, productPrice, availableCount, productType, status) " +
            "VALUES(#{productId}, #{productName}, #{productPrice}, #{availableCount}, #{productType}, #{status})")
    int insert(Product product);

    @Delete("DELETE FROM product WHERE productId = #{productId}")
    int deleteById(String productId);

    @Update("UPDATE product SET productName=#{productName}, productPrice=#{productPrice}, " +
            "availableCount=#{availableCount}, productType=#{productType}, status=#{status} WHERE productId=#{productId}")
    int update(Product product);

    @Update("UPDATE product SET status=#{status} WHERE productId=#{productId}")
    int updateStatus(@Param("productId") String productId, @Param("status") String status);

    @Select("SELECT * FROM product WHERE productId = #{productId}")
    Product selectById(String productId);

    @Select("SELECT * FROM product")
    List<Product> selectAll();

    @Select("SELECT * FROM product WHERE productType = #{productType}")
    List<Product> selectByType(String productType);

    @Update("UPDATE product SET availableCount = availableCount - #{quantity} " +
            "WHERE productId = #{productId} AND availableCount >= #{quantity}")
    int reduceStock(@Param("productId") String productId, @Param("quantity") Integer quantity);

    @Update("UPDATE product SET availableCount = availableCount + #{quantity} " +
            "WHERE productId = #{productId}")
    int increaseStock(@Param("productId") String productId, @Param("quantity") Integer quantity);

    @Select("SELECT * FROM product WHERE productName LIKE '%' || #{keyword} || '%' " +
            "OR productType LIKE '%' || #{keyword} || '%'")
    List<Product> searchProducts(@Param("keyword") String keyword);

    @Select("SELECT * FROM product WHERE availableCount <= #{threshold}")
    List<Product> selectLowStockProducts(@Param("threshold") Integer threshold);

    // 批量操作方法（使用XML实现）
    int insertBatch(List<Product> products);
    int deleteByIds(List<String> productIds);

    // 新增：分页查询方法（使用XML实现，带过滤）
    List<Product> selectPaged(@Param("offset") int offset,
                              @Param("size") int size,
                              @Param("orderBy") String orderBy,
                              @Param("status") String status,
                              @Param("search") String search);

    // 新增：统计总数
    @Select("SELECT COUNT(*) FROM product")
    long countAll();

    // 统计（带过滤，使用XML实现）
    long countByFilter(@Param("status") String status,
                       @Param("search") String search);
}
