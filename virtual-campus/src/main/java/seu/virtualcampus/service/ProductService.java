package seu.virtualcampus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seu.virtualcampus.domain.Product;
import seu.virtualcampus.mapper.ProductMapper;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    // ========== 您要求的核心方法 ==========

    public List<Product> getAllProducts() {
        return productMapper.selectAll();
    }

    // 新增：分页查询方法（可选过滤）
    public List<Product> getAllProducts(Integer page, Integer size, String sort) {
        return getAllProducts(page, size, sort, null, null);
    }

    // 重载：带状态与关键字过滤
    public List<Product> getAllProducts(Integer page, Integer size, String sort, String status, String search) {
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 处理排序参数
        String orderBy = parseSort(sort);
        
        return productMapper.selectPaged(offset, size, orderBy, normalizeStatus(status), normalizeSearch(search));
    }

    // 新增：统计总数
    public long countAllProducts() { return productMapper.countAll(); }

    // 统计（带过滤）
    public long countProducts(String status, String search) {
        return productMapper.countByFilter(normalizeStatus(status), normalizeSearch(search));
    }

    // 私有方法：解析排序参数，包含白名单验证
    private String parseSort(String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return "productName ASC"; // 默认排序
        }
        
        // 字段映射白名单
        java.util.Map<String, String> fieldMapping = new java.util.HashMap<>();
        fieldMapping.put("name", "productName");
        fieldMapping.put("price", "productPrice");
        fieldMapping.put("stock", "availableCount");
        fieldMapping.put("type", "productType");
        
        // 解析排序字符串 (例: "price,asc" 或 "name,desc")
        String[] parts = sort.split(",");
        if (parts.length != 2) {
            return "productName ASC"; // 格式错误，使用默认
        }
        
        String field = parts[0].trim();
        String direction = parts[1].trim().toUpperCase();
        
        // 验证字段白名单
        String mappedField = fieldMapping.get(field);
        if (mappedField == null) {
            return "productName ASC"; // 字段不在白名单，使用默认
        }
        
        // 验证排序方向
        if (!"ASC".equals(direction) && !"DESC".equals(direction)) {
            direction = "ASC"; // 方向无效，使用默认
        }
        
        return mappedField + " " + direction;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) return null;
        String s = status.trim().toUpperCase();
        if ("ON".equals(s) || "ACTIVE".equals(s)) return "ACTIVE";
        if ("OFF".equals(s) || "INACTIVE".equals(s)) return "INACTIVE";
        return null; // 非法值忽略
    }

    private String normalizeSearch(String search) {
        if (search == null) return null;
        String s = search.trim();
        return s.isEmpty() ? null : s;
    }

    public Product getProductById(String id) {
        return productMapper.selectById(id);
    }

    @Transactional
    public int addProduct(Product product) {
        // 自动生成ID和设置默认状态
        if (product.getProductId() == null || product.getProductId().isEmpty()) {
            product.setProductId(generateProductId());
        }
        if (product.getStatus() == null) {
            product.setStatus("ACTIVE");
        }
        return productMapper.insert(product);
    }

    @Transactional
    public int updateProduct(Product product) {
        return productMapper.update(product);
    }

    @Transactional
    public int deleteProduct(String id) {
        return productMapper.deleteById(id);
    }

    @Transactional
    public int changeProductStatus(String id, String status) {
        return productMapper.updateStatus(id, status);
    }

    // ========== 扩展功能方法 ==========

    public List<Product> getProductsByType(String productType) {
        return productMapper.selectByType(productType);
    }

    @Transactional
    public int reduceStock(String productId, Integer quantity) {
        return productMapper.reduceStock(productId, quantity);
    }

    @Transactional
    public int increaseStock(String productId, Integer quantity) {
        return productMapper.increaseStock(productId, quantity);
    }

    public List<Product> searchProducts(String keyword) {
        return productMapper.searchProducts(keyword);
    }

    public List<Product> getLowStockProducts(Integer threshold) {
        return productMapper.selectLowStockProducts(threshold);
    }

    @Transactional
    public int addProductsBatch(List<Product> products) {
        // 为批量商品设置默认值
        for (Product product : products) {
            if (product.getProductId() == null || product.getProductId().isEmpty()) {
                product.setProductId(generateProductId());
            }
            if (product.getStatus() == null) {
                product.setStatus("ACTIVE");
            }
        }
        return productMapper.insertBatch(products);
    }

    @Transactional
    public int removeProductsByIds(List<String> productIds) {
        return productMapper.deleteByIds(productIds);
    }

    // ========== 私有辅助方法 ==========

    private String generateProductId() {
        return "PROD" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }
}
