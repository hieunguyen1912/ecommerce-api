package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.CreateCategoryRequest;
import com.hieu.ecommerce.model.dto.request.UpdateCategoryRequest;
import com.hieu.ecommerce.model.dto.response.CategoryResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CreateCategoryRequest createCategoryRequest);

    CategoryResponse getCategoryById(Long id);

    CategoryResponse updateCategory(Long id, UpdateCategoryRequest updateCategoryRequest);

    void deleteCategory(Long id);

    Page<CategoryResponse> getAllCategories(Pageable pageable);
    
    List<CategoryResponse> getCategoryTree();
}
