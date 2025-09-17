package seu.virtualcampus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seu.virtualcampus.domain.Cart;
import seu.virtualcampus.service.CartService;
import java.util.List;
import java.util.Map;

/**
 * 购物车控制器。
 * <p>
 * 提供管理用户购物车相关的API接口，包括添加、删除、更新和查询购物车中的商品。
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    /**
     * 向购物车中添加一件商品。
     *
     * @param cart 购物车项目对象，包含用户ID、商品ID和数量等信息。
     * @return 操作结果的消息。
     */
    @PostMapping("/add")
    public ResponseEntity<String> addCartItem(@RequestBody Cart cart) {
        try {
            int result = cartService.addCartItem(cart);
            if (result > 0) {
                return ResponseEntity.ok("Cart item added successfully");
            } else {
                return ResponseEntity.badRequest().body("Failed to add cart item");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 从购物车中删除一件商品。
     *
     * @param cartItemId 要删除的购物车项目ID。
     * @return 操作结果的消息。
     */
    @DeleteMapping("/remove")
    public ResponseEntity<String> removeCartItem(@RequestParam String cartItemId) {
        int result = cartService.removeCartItem(cartItemId);
        if (result > 0) {
            return ResponseEntity.ok("Cart item removed successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to remove cart item");
        }
    }

    /**
     * 更新购物车中一件商品的信息（如数量）。
     *
     * @param cart 包含更新信息的购物车项目对象。
     * @return 操作结果的消息。
     */
    @PutMapping("/update")
    public ResponseEntity<String> updateCartItem(@RequestBody Cart cart) {
        int result = cartService.updateCartItem(cart);
        if (result > 0) {
            return ResponseEntity.ok("Cart item updated successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to update cart item");
        }
    }

    /**
     * 根据ID获取购物车中的一个项目。
     *
     * @param cartItemId 购物车项目ID。
     * @return 购物车项目详情；如果未找到则返回404。
     */
    @GetMapping("/get")
    public ResponseEntity<Cart> getCartItemById(@RequestParam String cartItemId) {
        Cart cartItem = cartService.getCartItemById(cartItemId);
        if (cartItem != null) {
            return ResponseEntity.ok(cartItem);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取指定用户的所有购物车项目。
     *
     * @param userId 用户ID。
     * @return 该用户的购物车项目列表。
     */
    @GetMapping("/user-items")
    public ResponseEntity<List<Cart>> getCartItemsByUserId(@RequestParam String userId) {
        List<Cart> cartItems = cartService.getCartItemsByUserId(userId);
        return ResponseEntity.ok(cartItems);
    }

    /**
     * 清空指定用户的购物车。
     *
     * @param userId 用户ID。
     * @return 操作结果的消息。
     */
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearUserCart(@RequestParam String userId) {
        int result = cartService.clearUserCart(userId);
        if (result > 0) {
            return ResponseEntity.ok("User cart cleared successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to clear user cart");
        }
    }

    /**
     * 批量删除购物车中的多个项目。
     *
     * @param cartItemIds 要删除的购物车项目ID列表。
     * @return 操作结果的消息。
     */
    @PostMapping("/batch-remove")
    public ResponseEntity<String> batchRemoveCartItems(@RequestBody List<String> cartItemIds) {
        int result = cartService.removeCartItemsByIds(cartItemIds);
        if (result > 0) {
            return ResponseEntity.ok("Cart items removed successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to remove cart items");
        }
    }

    /**
     * 获取用户购物车的摘要信息（例如，总价和商品总数）。
     *
     * @param userId 用户ID。
     * @return 包含购物车摘要信息的Map。
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getCartSummary(@RequestParam String userId) {
        Map<String, Object> summary = cartService.getCartSummary(userId);
        return ResponseEntity.ok(summary);
    }

    /**
     * 验证用户购物车中的商品状态（例如，库存是否充足）。
     *
     * @param userId 用户ID。
     * @return 包含验证结果的Map。
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateCart(@RequestParam String userId) {
        Map<String, Object> validation = cartService.validateCart(userId);
        return ResponseEntity.ok(validation);
    }

    // ========== 新的购物车接口 ==========

    /**
     * (新) 向购物车添加商品。
     *
     * @param userId 用户ID。
     * @param productId 商品ID。
     * @param quantity 商品数量。
     * @return 操作结果的消息。
     */
    @PostMapping("/add-item")
    public ResponseEntity<String> addItem(@RequestParam String userId, 
                                        @RequestParam String productId, 
                                        @RequestParam int quantity) {
        try {
            log.info("[CartController] addItem called. userId={}, productId={}, quantity={}", userId, productId, quantity);
            int result = cartService.addItem(userId, productId, quantity);
            if (result > 0) {
                log.info("[CartController] addItem success. affectedRows={}", result);
                return ResponseEntity.ok("商品添加到购物车成功");
            } else {
                log.warn("[CartController] addItem returned 0. userId={}, productId={}, quantity={}", userId, productId, quantity);
                return ResponseEntity.badRequest().body("添加商品失败");
            }
        } catch (Exception e) {
            log.error("[CartController] addItem error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("添加商品异常: " + e.getMessage());
        }
    }

    /**
     * (新) 更新购物车中商品的数量。
     *
     * @param userId 用户ID。
     * @param itemId 购物车项目ID。
     * @param quantity 更新后的商品数量。
     * @return 操作结果的消息。
     */
    @PutMapping("/{itemId}")
    public ResponseEntity<String> updateItemQuantity(@RequestParam String userId,
                                                    @PathVariable String itemId,
                                                    @RequestParam int quantity) {
        try {
            int result = cartService.updateItemQuantity(userId, itemId, quantity);
            if (result > 0) {
                return ResponseEntity.ok("购物车商品数量更新成功");
            } else {
                return ResponseEntity.badRequest().body("更新商品数量失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * (新) 从购物车中删除一个项目。
     *
     * @param userId 用户ID。
     * @param itemId 要删除的购物车项目ID。
     * @return 操作结果的消息。
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<String> removeItem(@RequestParam String userId,
                                           @PathVariable String itemId) {
        try {
            int result = cartService.removeItem(userId, itemId);
            if (result > 0) {
                return ResponseEntity.ok("商品从购物车删除成功");
            } else {
                return ResponseEntity.badRequest().body("删除商品失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * (新) 根据用户ID获取其购物车内容。
     *
     * @param userId 用户ID。
     * @return 用户的购物车项目列表。
     */
    @GetMapping
    public ResponseEntity<List<Cart>> getCartByUserId(@RequestParam String userId) {
        List<Cart> cartItems = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cartItems);
    }
}