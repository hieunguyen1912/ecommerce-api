package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.CategoryRequest;
import com.hieu.ecommerce.model.dto.response.CategoryResponse;

import com.hieu.ecommerce.model.entity.Category;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest createCategoryRequest);

    CategoryResponse getCategoryById(Long id);

    CategoryResponse updateCategory(Long id, CategoryRequest updateCategoryRequest);

    CategoryResponse deleteCategory(Long id);

    List<CategoryResponse> getAllCategories(Pageable pageable);

    List<Category> getCategoriesByIds(List<Long> categoryIds);
}
