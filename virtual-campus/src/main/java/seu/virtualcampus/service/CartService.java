package seu.virtualcampus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import seu.virtualcampus.domain.Cart;
import seu.virtualcampus.mapper.CartMapper;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartMapper cartMapper;

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
}