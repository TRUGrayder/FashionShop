package com.fashionshop.service;

import com.fashionshop.model.CartItem;
import java.math.BigDecimal;
import java.util.List;

public interface CartService {
    List<CartItem> getCartItems(Long userId);
    boolean addToCart(Long userId, Long productId, int quantity);
    boolean updateQuantity(Long userId, Long productId, int quantity);
    boolean removeItem(Long userId, Long productId);
    boolean clearCart(Long userId);
    int countItems(Long userId);
    BigDecimal getTotal(Long userId);
}
