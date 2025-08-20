package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.exception.ResourceNotFoundException;
import com.hieu.ecommerce.mapper.CategoryMapper;
import com.hieu.ecommerce.model.dto.request.CategoryRequest;
import com.hieu.ecommerce.model.dto.response.CategoryResponse;
import com.hieu.ecommerce.model.entity.Category;
import com.hieu.ecommerce.repository.CategoryRepository;
import com.hieu.ecommerce.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest createCategoryRequest) {
        if (categoryRepository.existsByName(createCategoryRequest.getName())) {
            throw new IllegalArgumentException("Category name already exists.");
        }

        if (createCategoryRequest.getParentId() != null && !categoryRepository.existsById(createCategoryRequest.getParentId())) {
            throw new ResourceNotFoundException("Parent category not found with id: " + createCategoryRequest.getParentId());
        }

        Category category = categoryMapper.toEntity(createCategoryRequest);
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        return categoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest updateCategoryRequest) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (!updateCategoryRequest.getName().equals(existingCategory.getName()) &&
            categoryRepository.existsByName(updateCategoryRequest.getName())) {
            throw new IllegalArgumentException("Category name already exists.");
        }

        if (updateCategoryRequest.getParentId() != null &&
                !updateCategoryRequest.getParentId().equals(existingCategory.getId())) {
            existingCategory.setParent(new Category(updateCategoryRequest.getParentId()));
            if (!categoryRepository.existsById(updateCategoryRequest.getParentId())) {
                throw new ResourceNotFoundException("Parent category not found with id: " + updateCategoryRequest.getParentId());
            }
        }

        existingCategory.setName(updateCategoryRequest.getName());
        existingCategory.setDescription(updateCategoryRequest.getDescription());

        Category updatedCategory = categoryRepository.save(existingCategory);

        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    public CategoryResponse deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        categoryRepository.delete(category);

        return categoryMapper.toResponse(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public List<Category> getCategoriesByIds(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Category> categories = categoryRepository.findAllById(categoryIds);

        if (categories.size() != categoryIds.size()) {
            Set<Long> foundIds = categories.stream().map(Category::getId).collect(Collectors.toSet());
            List<Long> missingIds = categoryIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new ResourceNotFoundException("Categories not found with IDs: " + missingIds);
        }

        return new ArrayList<>(categories);
    }
}
