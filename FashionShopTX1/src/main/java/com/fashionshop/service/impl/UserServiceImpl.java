package com.fashionshop.service.impl;

import com.fashionshop.dao.UserDAO;
import com.fashionshop.model.User;
import com.fashionshop.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;

    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User login(String username, String password) {
        User user = userDAO.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    @Override
    public User findByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    @Override
    public User findById(Long id) {
        return userDAO.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    @Override
    public boolean register(User user) {
        if (userDAO.findByUsername(user.getUsername()) != null) {
            return false; // username đã tồn tại
        }
        return userDAO.insert(user);
    }

    @Override
    public boolean updateProfile(User user) {
        return userDAO.update(user);
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userDAO.findById(userId);
        if (user != null && user.getPassword().equals(oldPassword)) {
            return userDAO.updatePassword(userId, newPassword);
        }
        return false;
    }
}
