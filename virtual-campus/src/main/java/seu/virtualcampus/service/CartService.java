package seu.virtualcampus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import seu.virtualcampus.domain.Cart;
import seu.virtualcampus.domain.Product;
import seu.virtualcampus.mapper.CartMapper;
import seu.virtualcampus.mapper.ProductMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    public int addCartItem(Cart cart) {
        // 检查是否已存在相同商品
        Cart existingItem = cartMapper.selectByUserAndProduct(
                cart.getUserId(), cart.getProductId());

        if (existingItem != null) {
            // 如果已存在，更新数量
            existingItem.setQuantity(existingItem.getQuantity() + cart.getQuantity());
            return cartMapper.update(existingItem);
        } else {
            // 如果不存在，添加新项
            return cartMapper.insert(cart);
        }
    }

    public int removeCartItem(String cartItemId) {
        return cartMapper.deleteById(cartItemId);
    }

    public int updateCartItem(Cart cart) {
        return cartMapper.update(cart);
    }

    public Cart getCartItemById(String cartItemId) {
        return cartMapper.selectById(cartItemId);
    }

    public List<Cart> getCartItemsByUserId(String userId) {
        return cartMapper.selectByUserId(userId);
    }

    public Cart getCartItemByUserAndProduct(String userId, String productId) {
        return cartMapper.selectByUserAndProduct(userId, productId);
    }

    public int clearUserCart(String userId) {
        return cartMapper.deactivateAllByUserId(userId);
    }

    public List<Cart> getCartItemsByIds(List<String> cartItemIds) {
        return cartMapper.selectByIds(cartItemIds);
    }

    public int removeCartItemsByIds(List<String> cartItemIds) {
        return cartMapper.deleteByIds(cartItemIds);
    }

    /**
     * 添加商品到购物车
     */
    public int addItem(String userId, String productId, int quantity) {
        log.info("[CartService] addItem start. userId={}, productId={}, quantity={}", userId, productId, quantity);
        // 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            log.warn("[CartService] product not found. productId={}", productId);
            throw new RuntimeException("商品不存在");
        }

        // 检查是否已存在相同商品
        Cart existingItem = cartMapper.selectByUserAndProduct(userId, productId);
        
        if (existingItem != null) {
            // 如果已存在，更新数量
            int newQuantity = existingItem.getQuantity() + quantity;
            int rows = cartMapper.updateQuantity(existingItem.getCartItemId(), newQuantity);
            log.info("[CartService] existing cart item. cartItemId={}, oldQty={}, newQty={}, affectedRows={}",
                    existingItem.getCartItemId(), existingItem.getQuantity(), newQuantity, rows);
            return rows;
        } else {
            // 如果不存在，添加新项
            Cart cart = new Cart();
            cart.setCartItemId(generateCartItemId());
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            cart.setIsActive(1);
            int rows = cartMapper.insert(cart);
            log.info("[CartService] inserted new cart item. cartItemId={}, affectedRows={}", cart.getCartItemId(), rows);
            return rows;
        }
    }

    /**
     * 更新购物车商品数量
     */
    public int updateItemQuantity(String userId, String cartItemId, int quantity) {
        // 验证购物车项是否属于该用户
        Cart cart = cartMapper.selectById(cartItemId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new RuntimeException("购物车项不存在或无权限操作");
        }

        if (quantity <= 0) {
            // 数量为0或负数时删除该项
            return cartMapper.deleteById(cartItemId);
        } else {
            return cartMapper.updateQuantity(cartItemId, quantity);
        }
    }

    /**
     * 删除购物车商品
     */
    public int removeItem(String userId, String cartItemId) {
        // 验证购物车项是否属于该用户
        Cart cart = cartMapper.selectById(cartItemId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new RuntimeException("购物车项不存在或无权限操作");
        }

        return cartMapper.deleteById(cartItemId);
    }

    /**
     * 获取用户购物车
     */
    public List<Cart> getCartByUserId(String userId) {
        return cartMapper.selectByUserId(userId);
    }

    /**
     * 生成购物车项ID
     */
    private String generateCartItemId() {
        return "CART" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }

    /**
     * 获取购物车摘要统计
     */
    public Map<String, Object> getCartSummary(String userId) {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            List<Cart> cartItems = cartMapper.selectByUserId(userId);
            
            int totalQuantity = 0;
            BigDecimal totalPrice = BigDecimal.ZERO;
            int validItems = 0;
            
            for (Cart cart : cartItems) {
                Product product = productMapper.selectById(cart.getProductId());
                if (product != null) {
                    totalQuantity += cart.getQuantity();
                    BigDecimal itemTotal = BigDecimal.valueOf(product.getProductPrice())
                        .multiply(BigDecimal.valueOf(cart.getQuantity()));
                    totalPrice = totalPrice.add(itemTotal);
                    validItems++;
                }
            }
            
            summary.put("success", true);
            summary.put("totalItems", cartItems.size());
            summary.put("validItems", validItems);
            summary.put("totalQuantity", totalQuantity);
            summary.put("totalPrice", totalPrice.setScale(2, RoundingMode.HALF_UP));
            
        } catch (Exception e) {
            summary.put("success", false);
            summary.put("message", "获取购物车统计失败: " + e.getMessage());
        }
        
        return summary;
    }

    /**
     * 验证购物车项的库存和价格
     */
    public Map<String, Object> validateCart(String userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Cart> cartItems = cartMapper.selectByUserId(userId);
            boolean isValid = true;
            StringBuilder issues = new StringBuilder();
            
            for (Cart cart : cartItems) {
                Product product = productMapper.selectById(cart.getProductId());
                
                if (product == null) {
                    isValid = false;
                    issues.append("商品不存在: ").append(cart.getProductId()).append("; ");
                } else if (product.getAvailableCount() < cart.getQuantity()) {
                    isValid = false;
                    issues.append("库存不足: ").append(product.getProductName())
                           .append(" (需要").append(cart.getQuantity())
                           .append("，库存").append(product.getAvailableCount()).append("); ");
                }
            }
            
            result.put("success", true);
            result.put("isValid", isValid);
            result.put("issues", issues.toString());
            result.put("validatedItems", cartItems.size());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "验证购物车失败: " + e.getMessage());
        }
        
        return result;
    }
}
