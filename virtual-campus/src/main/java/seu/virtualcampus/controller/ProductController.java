package seu.virtualcampus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seu.virtualcampus.domain.Product;
import seu.virtualcampus.service.ProductService;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // ========== 用户端接口（浏览商品） ==========

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

    @PostMapping
    public ResponseEntity<String> addProduct(@RequestBody Product product) {
        int result = productService.addProduct(product);
        if (result > 0) {
            return ResponseEntity.ok("Product added successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to add product");
        }
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable String id) {
        int result = productService.deleteProduct(id);
        if (result > 0) {
            return ResponseEntity.ok("Product deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to delete product");
        }
    }

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

    @GetMapping("/by-type")
    public ResponseEntity<List<Product>> getProductsByType(@RequestParam String productType) {
        List<Product> products = productService.getProductsByType(productType);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        List<Product> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts(@RequestParam(defaultValue = "10") Integer threshold) {
        List<Product> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }

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

    @PostMapping("/batch")
    public ResponseEntity<String> addProductsBatch(@RequestBody List<Product> products) {
        int result = productService.addProductsBatch(products);
        if (result > 0) {
            return ResponseEntity.ok("Products added successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to add products");
        }
    }

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
