package seu.virtualcampus.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * 购物车服务类。
 * <p>
 * 提供购物车相关的增删改查、商品校验、批量操作等业务逻辑。
 */
@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    /**
     * 添加购物车项。
     *
     * @param cart 购物车对象。
     * @return 操作影响的行数。
     * @throws RuntimeException 商品不存在或数量非法、库存不足时抛出。
     */
    public int addCartItem(Cart cart) {
        // 基础商品校验与库存校验
        Product product = productMapper.selectById(cart.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (cart.getQuantity() == null || cart.getQuantity() <= 0) {
            throw new RuntimeException("购买数量必须大于0");
        }

        // 检查是否已存在相同商品
        Cart existingItem = cartMapper.selectByUserAndProduct(
                cart.getUserId(), cart.getProductId());

        int currentQty = existingItem != null ? existingItem.getQuantity() : 0;
        int desiredTotal = currentQty + cart.getQuantity();
        if (product.getAvailableCount() == null || desiredTotal > product.getAvailableCount()) {
            int stock = product.getAvailableCount() == null ? 0 : product.getAvailableCount();
            throw new RuntimeException("库存不足：剩余 " + stock + " 件");
        }

        if (existingItem != null) {
            // 如果已存在，更新数量
            existingItem.setQuantity(desiredTotal);
            return cartMapper.update(existingItem);
        } else {
            // 如果不存在，添加新项
            return cartMapper.insert(cart);
        }
    }

    /**
     * 移除指定购物车项。
     *
     * @param cartItemId 购物车项ID。
     * @return 操作影响的行数。
     */
    public int removeCartItem(String cartItemId) {
        return cartMapper.deleteById(cartItemId);
    }

    /**
     * 更新购物车项。
     *
     * @param cart 购物车对象。
     * @return 操作影响的行数。
     */
    public int updateCartItem(Cart cart) {
        return cartMapper.update(cart);
    }

    /**
     * 根据ID获取购物车项。
     *
     * @param cartItemId 购物车项ID。
     * @return 对应的购物车项对象，若不存在则返回null。
     */
    public Cart getCartItemById(String cartItemId) {
        return cartMapper.selectById(cartItemId);
    }

    /**
     * 根据用户ID获取所有购物车项。
     *
     * @param userId 用户ID。
     * @return 该用户的所有购物车项列表。
     */
    public List<Cart> getCartItemsByUserId(String userId) {
        return cartMapper.selectByUserId(userId);
    }

    /**
     * 根据用户ID和商品ID获取购物车项。
     *
     * @param userId    用户ID。
     * @param productId 商品ID。
     * @return 对应的购物车项对象，若不存在则返回null。
     */
    public Cart getCartItemByUserAndProduct(String userId, String productId) {
        return cartMapper.selectByUserAndProduct(userId, productId);
    }

    /**
     * 清空用户购物车。
     *
     * @param userId 用户ID。
     * @return 操作影响的行数。
     */
    public int clearUserCart(String userId) {
        return cartMapper.deactivateAllByUserId(userId);
    }

    /**
     * 根据购物车项ID列表批量获取购物车项。
     *
     * @param cartItemIds 购物车项ID列表。
     * @return 对应的购物车项列表。
     */
    public List<Cart> getCartItemsByIds(List<String> cartItemIds) {
        return cartMapper.selectByIds(cartItemIds);
    }

    /**
     * 根据购物车项ID列表批量移除购物车项。
     *
     * @param cartItemIds 购物车项ID列表。
     * @return 操作影响的行数。
     */
    public int removeCartItemsByIds(List<String> cartItemIds) {
        return cartMapper.deleteByIds(cartItemIds);
    }

    /**
     * 添加商品到购物车。
     *
     * @param userId    用户ID。
     * @param productId 商品ID。
     * @param quantity  购买数量。
     * @return 操作影响的行数。
     * @throws RuntimeException 商品不存在或库存不足时抛出。
     */
    public int addItem(String userId, String productId, int quantity) {
        log.info("[CartService] addItem start. userId={}, productId={}, quantity={}", userId, productId, quantity);
        // 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            log.warn("[CartService] product not found. productId={}", productId);
            throw new RuntimeException("商品不存在");
        }

        if (quantity <= 0) {
            throw new RuntimeException("购买数量必须大于0");
        }

        // 检查是否已存在相同商品
        Cart existingItem = cartMapper.selectByUserAndProduct(userId, productId);
        int currentQty = existingItem != null ? existingItem.getQuantity() : 0;
        int desiredTotal = currentQty + quantity;

        // 库存校验：购物车内同款总量不能超过库存
        Integer stock = product.getAvailableCount() == null ? 0 : product.getAvailableCount();
        if (desiredTotal > stock) {
            log.warn("[CartService] stock not enough. productId={}, stock={}, currentInCart={}, requestAdd={}",
                    productId, stock, currentQty, quantity);
            throw new RuntimeException("库存不足：剩余 " + stock + " 件");
        }

        if (existingItem != null) {
            // 如果已存在，更新数量
            int newQuantity = desiredTotal;
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
            // 库存校验
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在");
            }
            Integer stock = product.getAvailableCount() == null ? 0 : product.getAvailableCount();
            if (quantity > stock) {
                throw new RuntimeException("库存不足：剩余 " + stock + " 件");
            }
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
        return "CART" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
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
