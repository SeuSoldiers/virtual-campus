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

    @PostMapping("/add")
    public ResponseEntity<String> addProduct(@RequestBody Product product) {
        int result = productService.addProduct(product);
        if (result > 0) {
            return ResponseEntity.ok("Product added successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to add product");
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> removeProduct(@RequestParam String productId) {
        int result = productService.removeProduct(productId);
        if (result > 0) {
            return ResponseEntity.ok("Product removed successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to remove product");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateProduct(@RequestBody Product product) {
        int result = productService.updateProduct(product);
        if (result > 0) {
            return ResponseEntity.ok("Product updated successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to update product");
        }
    }

    @GetMapping("/get")
    public ResponseEntity<Product> getProductById(@RequestParam String productId) {
        Product product = productService.getProductById(productId);
        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/by-type")
    public ResponseEntity<List<Product>> getProductsByType(@RequestParam String productType) {
        List<Product> products = productService.getProductsByType(productType);
        return ResponseEntity.ok(products);
    }
}