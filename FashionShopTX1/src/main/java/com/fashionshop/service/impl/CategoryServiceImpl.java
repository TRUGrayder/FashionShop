package com.fashionshop.service.impl;

import com.fashionshop.dao.CategoryDAO;
import com.fashionshop.model.Category;
import com.fashionshop.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDAO categoryDAO;

    public CategoryServiceImpl(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryDAO.findById(id);
    }
}
