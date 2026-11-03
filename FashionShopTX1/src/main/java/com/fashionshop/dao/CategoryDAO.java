package com.fashionshop.dao;

import com.fashionshop.model.Category;
import java.util.List;

public interface CategoryDAO {
    List<Category> findAll();
    Category findById(Long id);
}
