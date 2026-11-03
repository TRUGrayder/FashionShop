package com.fashionshop.service;

import com.fashionshop.model.User;
import java.util.List;

public interface UserService {
    User login(String username, String password);
    User findByUsername(String username);
    User findById(Long id);
    List<User> getAllUsers();
    boolean register(User user);
    boolean updateProfile(User user);
    boolean changePassword(Long userId, String oldPassword, String newPassword);
}
