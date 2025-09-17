package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.Product;
import java.util.List;

/**
 * 商品Mapper接口。
 * <p>
 * 定义了与数据库中product表相关的操作。
 */
@Mapper
public interface ProductMapper {

    /**
     * 插入一个新商品。
     *
     * @param product 要插入的商品对象。
     * @return 受影响的行数。
     */
    @Insert("INSERT INTO product(productId, productName, productPrice, availableCount, productType, status) " +
            "VALUES(#{productId}, #{productName}, #{productPrice}, #{availableCount}, #{productType}, #{status})")
    int insert(Product product);

    /**
     * 根据ID删除一个商品。
     *
     * @param productId 要删除的商品ID。
     * @return 受影响的行数。
     */
    @Delete("DELETE FROM product WHERE productId = #{productId}")
    int deleteById(String productId);

    /**
     * 更新一个商品的完整信息。
     *
     * @param product 包含更新信息的商品对象。
     * @return 受影响的行数。
     */
    @Update("UPDATE product SET productName=#{productName}, productPrice=#{productPrice}, " +
            "availableCount=#{availableCount}, productType=#{productType}, status=#{status} WHERE productId=#{productId}")
    int update(Product product);

    /**
     * 更新一个商品的状态。
     *
     * @param productId 商品ID。
     * @param status    新的状态。
     * @return 受影响的行数。
     */
    @Update("UPDATE product SET status=#{status} WHERE productId=#{productId}")
    int updateStatus(@Param("productId") String productId, @Param("status") String status);

    /**
     * 根据ID查询一个商品。
     *
     * @param productId 商品ID。
     * @return 对应的商品对象，如果不存在则返回null。
     */
    @Select("SELECT * FROM product WHERE productId = #{productId}")
    Product selectById(String productId);

    /**
     * 查询所有商品。
     *
     * @return 数据库中所有商品的列表。
     */
    @Select("SELECT * FROM product")
    List<Product> selectAll();

    /**
     * 根据商品类型查询商品。
     *
     * @param productType 商品类型。
     * @return 该类型下的所有商品列表。
     */
    @Select("SELECT * FROM product WHERE productType = #{productType}")
    List<Product> selectByType(String productType);

    /**
     * 扣减商品库存。
     * <p>
     * 这是一个原子操作，只有在库存充足时才会成功。
     *
     * @param productId 商品ID。
     * @param quantity  要扣减的数量。
     * @return 受影响的行数（1表示成功，0表示失败）。
     */
    @Update("UPDATE product SET availableCount = availableCount - #{quantity} " +
            "WHERE productId = #{productId} AND availableCount >= #{quantity}")
    int reduceStock(@Param("productId") String productId, @Param("quantity") Integer quantity);

    /**
     * 增加商品库存。
     *
     * @param productId 商品ID。
     * @param quantity  要增加的数量。
     * @return 受影响的行数。
     */
    @Update("UPDATE product SET availableCount = availableCount + #{quantity} " +
            "WHERE productId = #{productId}")
    int increaseStock(@Param("productId") String productId, @Param("quantity") Integer quantity);

    /**
     * 根据关键词搜索商品。
     *
     * @param keyword 搜索关键词，匹配商品名称或类型。
     * @return 匹配的商品列表。
     */
    @Select("SELECT * FROM product WHERE productName LIKE '%' || #{keyword} || '%' " +
            "OR productType LIKE '%' || #{keyword} || '%'")
    List<Product> searchProducts(@Param("keyword") String keyword);

    /**
     * 查询库存量低于指定阈值的商品。
     *
     * @param threshold 库存阈值。
     * @return 低库存商品列表。
     */
    @Select("SELECT * FROM product WHERE availableCount <= #{threshold}")
    List<Product> selectLowStockProducts(@Param("threshold") Integer threshold);

    /**
     * 批量插入商品。
     * (此方法的SQL实现在XML映射文件中定义)
     *
     * @param products 要批量插入的商品列表。
     * @return 受影响的行数。
     */
    int insertBatch(List<Product> products);
    /**
     * 根据ID列表批量删除商品。
     * (此方法的SQL实现在XML映射文件中定义)
     *
     * @param productIds 要删除的商品ID列表。
     * @return 受影响的行数。
     */
    int deleteByIds(List<String> productIds);

    /**
     * 分页并过滤查询商品。
     * (此方法的SQL实现在XML映射文件中定义)
     *
     * @param offset  分页偏移量。
     * @param size    每页大小。
     * @param orderBy 排序条件。
     * @param status  状态过滤器。
     * @param search  搜索关键词。
     * @return 分页和过滤后的商品列表。
     */
    List<Product> selectPaged(@Param("offset") int offset,
                              @Param("size") int size,
                              @Param("orderBy") String orderBy,
                              @Param("status") String status,
                              @Param("search") String search);

    /**
     * 统计所有商品的总数。
     *
     * @return 商品总数。
     */
    @Select("SELECT COUNT(*) FROM product")
    long countAll();

    /**
     * 根据过滤器统计商品数量。
     * (此方法的SQL实现在XML映射文件中定义)
     *
     * @param status 状态过滤器。
     * @param search 搜索关键词。
     * @return 符合条件的商品总数。
     */
    long countByFilter(@Param("status") String status,
                       @Param("search") String search);
}