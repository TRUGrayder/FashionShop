package com.fashionshop.service;

import com.fashionshop.model.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    Category getCategoryById(Long id);
}
