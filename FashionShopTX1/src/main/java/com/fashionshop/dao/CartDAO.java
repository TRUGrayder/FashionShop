package com.fashionshop.dao;

import com.fashionshop.model.CartItem;
import java.util.List;

public interface CartDAO {
    List<CartItem> findByUserId(Long userId);
    CartItem findByUserAndProduct(Long userId, Long productId);
    boolean addItem(Long userId, Long productId, int quantity);
    boolean updateQuantity(Long userId, Long productId, int quantity);
    boolean removeItem(Long userId, Long productId);
    boolean clearCart(Long userId);
    int countItems(Long userId);
}
