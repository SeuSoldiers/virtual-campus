package seu.virtualcampus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seu.virtualcampus.domain.Cart;
import seu.virtualcampus.service.CartService;
import java.util.List;

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
}