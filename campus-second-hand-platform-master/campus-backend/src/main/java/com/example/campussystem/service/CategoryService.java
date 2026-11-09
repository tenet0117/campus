package com.example.campussystem.service;

import com.example.campussystem.entity.Category;

import java.util.List;

public interface CategoryService {

    Category createCategory(String name, Long parentId, Integer sortOrder);

    Category updateCategory(Long id, String name, Long parentId, Integer sortOrder, Integer status);

    void deleteCategory(Long id);

    Category getCategoryById(Long id);

    List<Category> getAllCategories();

    List<Category> getAllActiveCategories();

    List<Category> getTopLevelCategories();

    List<Category> getCategoriesByParentId(Long parentId);

    List<Category> getCategoryTree();

    Category toggleCategoryStatus(Long id);

    void deleteCategoriesBatch(List<Long> ids);

    void initializeBasicCategories();
}
