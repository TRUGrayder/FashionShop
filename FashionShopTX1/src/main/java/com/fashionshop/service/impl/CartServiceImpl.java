package com.fashionshop.service.impl;

import com.fashionshop.dao.CartDAO;
import com.fashionshop.model.CartItem;
import com.fashionshop.service.CartService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final CartDAO cartDAO;

    public CartServiceImpl(CartDAO cartDAO) {
        this.cartDAO = cartDAO;
    }

    @Override
    public List<CartItem> getCartItems(Long userId) {
        return cartDAO.findByUserId(userId);
    }

    @Override
    public boolean addToCart(Long userId, Long productId, int quantity) {
        return cartDAO.addItem(userId, productId, quantity);
    }

    @Override
    public boolean updateQuantity(Long userId, Long productId, int quantity) {
        return cartDAO.updateQuantity(userId, productId, quantity);
    }

    @Override
    public boolean removeItem(Long userId, Long productId) {
        return cartDAO.removeItem(userId, productId);
    }

    @Override
    public boolean clearCart(Long userId) {
        return cartDAO.clearCart(userId);
    }

    @Override
    public int countItems(Long userId) {
        return cartDAO.countItems(userId);
    }

    @Override
    public BigDecimal getTotal(Long userId) {
        List<CartItem> items = cartDAO.findByUserId(userId);
        return items.stream()
                .map(CartItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
