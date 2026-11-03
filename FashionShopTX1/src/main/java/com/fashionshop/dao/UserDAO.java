package com.fashionshop.dao;

import com.fashionshop.model.User;
import java.util.List;

public interface UserDAO {
    User findByUsername(String username);
    User findById(Long id);
    List<User> findAll();
    boolean insert(User user);
    boolean update(User user);
    boolean updatePassword(Long userId, String newPassword);
    boolean delete(Long id);
}
