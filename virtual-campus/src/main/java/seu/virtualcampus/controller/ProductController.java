package seu.virtualcampus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seu.virtualcampus.domain.Product;
import seu.virtualcampus.service.ProductService;
import java.util.List;

/**
 * 商品控制器。
 * <p>
 * 提供与商品相关的API接口，包括面向用户的商品浏览和搜索，以及面向管理员的商品增、删、改、查和库存管理功能。
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // ========== 用户端接口（浏览商品） ==========

    /**
     * 获取所有商品（分页、排序、过滤）。
     * <p>
     * 这是一个通用的商品列表查询接口，支持分页、排序、状态过滤和关键词搜索。
     *
     * @param page   页码，默认为1。
     * @param size   每页大小，默认为10。
     * @param sort   排序字段和方向，格式为 "字段名,asc/desc"，默认为 "productName,asc"。
     * @param search 搜索关键词，匹配商品名称或描述（可选）。
     * @param status 商品状态，可以是 "ACTIVE", "INACTIVE", "ON", "OFF", "ALL"（可选）。
     * @return 商品列表，响应头中包含 "X-Total-Count" 表示总记录数。
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "productName,asc") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {

        // 参数边界检查
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;

        // 统一状态取值（支持 ON/OFF 与 ACTIVE/INACTIVE）
        String normalizedStatus = null;
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            String s = status.trim().toUpperCase();
            if ("ON".equals(s) || "ACTIVE".equals(s)) normalizedStatus = "ACTIVE";
            else if ("OFF".equals(s) || "INACTIVE".equals(s)) normalizedStatus = "INACTIVE";
        }

        // 使用带过滤的分页查询
        List<Product> products = productService.getAllProducts(page, size, sort, normalizedStatus, search);
        long total = productService.countProducts(normalizedStatus, search);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(total))
                .body(products);
    }

    /**
     * 根据ID获取单个商品的详细信息。
     *
     * @param id 商品ID。
     * @return 商品详细信息；如果未找到则返回404。
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        Product product = productService.getProductById(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== 管理员端接口（商品管理） ==========

    /**
     * (管理员) 添加一个新商品。
     *
     * @param product 要添加的商品对象。
     * @return 操作结果的消息。
     */
    @PostMapping
    public ResponseEntity<String> addProduct(@RequestBody Product product) {
        int result = productService.addProduct(product);
        if (result > 0) {
            return ResponseEntity.ok("Product added successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to add product");
        }
    }

    /**
     * (管理员) 更新一个商品的信息。
     *
     * @param id      要更新的商品ID。
     * @param product 包含新信息的商品对象。
     * @return 操作结果的消息。
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable String id, @RequestBody Product product) {
        product.setProductId(id); // 确保ID一致
        int result = productService.updateProduct(product);
        if (result > 0) {
            return ResponseEntity.ok("Product updated successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to update product");
        }
    }

    /**
     * (管理员) 删除一个商品。
     *
     * @param id 要删除的商品ID。
     * @return 操作结果的消息。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable String id) {
        int result = productService.deleteProduct(id);
        if (result > 0) {
            return ResponseEntity.ok("Product deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to delete product");
        }
    }

    /**
     * (管理员) 更改一个商品的状态（例如，上架/下架）。
     *
     * @param id     商品ID。
     * @param status 新的状态。
     * @return 操作结果的消息。
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<String> changeProductStatus(@PathVariable String id, @RequestParam String status) {
        int result = productService.changeProductStatus(id, status);
        if (result > 0) {
            return ResponseEntity.ok("Product status changed successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to change product status");
        }
    }

    // ========== 扩展功能接口 ==========

    /**
     * 根据商品类型获取商品列表。
     *
     * @param productType 商品类型。
     * @return 该类型下的所有商品列表。
     */
    @GetMapping("/by-type")
    public ResponseEntity<List<Product>> getProductsByType(@RequestParam String productType) {
        List<Product> products = productService.getProductsByType(productType);
        return ResponseEntity.ok(products);
    }

    /**
     * 根据关键词搜索商品。
     *
     * @param keyword 搜索关键词。
     * @return 匹配的商品列表。
     */
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        List<Product> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(products);
    }

    /**
     * (管理员) 获取低库存商品列表。
     *
     * @param threshold 库存阈值，低于此值的商品将被返回。默认为10。
     * @return 低库存商品列表。
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts(@RequestParam(defaultValue = "10") Integer threshold) {
        List<Product> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }

    /**
     * (管理员) 增加指定商品的库存。
     *
     * @param productId 商品ID。
     * @param quantity  要增加的库存数量。
     * @return 操作结果的消息。
     */
    @PutMapping("/stock/increase")
    public ResponseEntity<String> increaseStock(@RequestParam String productId, 
                                               @RequestParam Integer quantity) {
        int result = productService.increaseStock(productId, quantity);
        if (result > 0) {
            return ResponseEntity.ok("Stock increased successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to increase stock");
        }
    }

    /**
     * (管理员) 批量添加商品。
     *
     * @param products 要添加的商品列表。
     * @return 操作结果的消息。
     */
    @PostMapping("/batch")
    public ResponseEntity<String> addProductsBatch(@RequestBody List<Product> products) {
        int result = productService.addProductsBatch(products);
        if (result > 0) {
            return ResponseEntity.ok("Products added successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to add products");
        }
    }

    /**
     * (管理员) 批量删除商品。
     *
     * @param productIds 要删除的商品ID列表。
     * @return 操作结果的消息。
     */
    @DeleteMapping("/batch")
    public ResponseEntity<String> removeProductsByIds(@RequestBody List<String> productIds) {
        int result = productService.removeProductsByIds(productIds);
        if (result > 0) {
            return ResponseEntity.ok("Products removed successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to remove products");
        }
    }
}