package seu.virtualcampus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seu.virtualcampus.domain.Cart;
import seu.virtualcampus.service.CartService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<String> addCartItem(@RequestBody Cart cart) {
        int result = cartService.addCartItem(cart);
        if (result > 0) {
            return ResponseEntity.ok("Cart item added successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to add cart item");
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> removeCartItem(@RequestParam String cartItemId) {
        int result = cartService.removeCartItem(cartItemId);
        if (result > 0) {
            return ResponseEntity.ok("Cart item removed successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to remove cart item");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateCartItem(@RequestBody Cart cart) {
        int result = cartService.updateCartItem(cart);
        if (result > 0) {
            return ResponseEntity.ok("Cart item updated successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to update cart item");
        }
    }

    @GetMapping("/get")
    public ResponseEntity<Cart> getCartItemById(@RequestParam String cartItemId) {
        Cart cartItem = cartService.getCartItemById(cartItemId);
        if (cartItem != null) {
            return ResponseEntity.ok(cartItem);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user-items")
    public ResponseEntity<List<Cart>> getCartItemsByUserId(@RequestParam String userId) {
        List<Cart> cartItems = cartService.getCartItemsByUserId(userId);
        return ResponseEntity.ok(cartItems);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearUserCart(@RequestParam String userId) {
        int result = cartService.clearUserCart(userId);
        if (result > 0) {
            return ResponseEntity.ok("User cart cleared successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to clear user cart");
        }
    }

    @PostMapping("/batch-remove")
    public ResponseEntity<String> batchRemoveCartItems(@RequestBody List<String> cartItemIds) {
        int result = cartService.removeCartItemsByIds(cartItemIds);
        if (result > 0) {
            return ResponseEntity.ok("Cart items removed successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to remove cart items");
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getCartSummary(@RequestParam String userId) {
        Map<String, Object> summary = cartService.getCartSummary(userId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateCart(@RequestParam String userId) {
        Map<String, Object> validation = cartService.validateCart(userId);
        return ResponseEntity.ok(validation);
    }

    // ========== 新的购物车接口 ==========

    @PostMapping("/add-item")
    public ResponseEntity<String> addItem(@RequestParam String userId, 
                                        @RequestParam String productId, 
                                        @RequestParam int quantity) {
        try {
            int result = cartService.addItem(userId, productId, quantity);
            if (result > 0) {
                return ResponseEntity.ok("商品添加到购物车成功");
            } else {
                return ResponseEntity.badRequest().body("添加商品失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

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

    @GetMapping
    public ResponseEntity<List<Cart>> getCartByUserId(@RequestParam String userId) {
        List<Cart> cartItems = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cartItems);
    }
}