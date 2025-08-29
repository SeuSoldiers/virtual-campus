package seu.virtualcampus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import seu.virtualcampus.domain.Product;
import seu.virtualcampus.mapper.ProductMapper;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    public int addProduct(Product product) {
        return productMapper.insert(product);
    }

    public int removeProduct(String productId) {
        return productMapper.deleteById(productId);
    }

    public int updateProduct(Product product) {
        return productMapper.update(product);
    }

    public Product getProductById(String productId) {
        return productMapper.selectById(productId);
    }

    public List<Product> getAllProducts() {
        return productMapper.selectAll();
    }

    public List<Product> getProductsByType(String productType) {
        return productMapper.selectByType(productType);
    }

    public int reduceStock(String productId, Integer quantity) {
        return productMapper.reduceStock(productId, quantity);
    }
}