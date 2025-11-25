package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.constant.ErrorCode;
import com.hieu.ecommerce.exception.AppException;
import com.hieu.ecommerce.mapper.CategoryMapper;
import com.hieu.ecommerce.model.dto.request.CreateCategoryRequest;
import com.hieu.ecommerce.model.dto.request.UpdateCategoryRequest;
import com.hieu.ecommerce.model.dto.response.CategoryResponse;
import com.hieu.ecommerce.model.entity.Category;
import com.hieu.ecommerce.repository.CategoryRepository;
import com.hieu.ecommerce.service.CategoryService;
import com.hieu.ecommerce.util.SlugHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.info("Creating category with name: {}", request.getName());

        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.DUPLICATE_RESOURCE);
        }

        String slug = SlugHelper.generateSlug(request.getName());

        String originalSlug = slug;
        int counter = 1;
        while (categoryRepository.existsBySlugIgnoreCase(slug)) {
            slug = originalSlug + "-" + counter;
            counter++;
        }

        Category category = categoryMapper.toEntity(request);
        category.setSlug(slug);

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findByIdWithChildren(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Category not found with id: " + id));

        return categoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        log.info("Updating category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (request.getName() != null && !request.getName().equals(category.getName())
                && categoryRepository.existsByNameIgnoreCase(request.getName())) {
                throw new AppException(ErrorCode.DUPLICATE_RESOURCE);
            }


        categoryMapper.update(request, category);
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(
                            () -> new AppException(ErrorCode.DUPLICATE_RESOURCE)
                    );
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        Category updatedCategory = categoryRepository.save(category);

        log.info("Successfully updated category with ID: {}", updatedCategory.getId());
        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Category not found with id: " + id));

        categoryRepository.delete(category);
    }

    @Override
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        Page<Category> categoryPage = categoryRepository.findAllRootCategories(pageable);
        return categoryPage.map(categoryMapper::toResponse);
    }

    @Override
    public List<CategoryResponse> getCategoryTree() {
        log.info("Fetching category tree");
        
        List<Category> allCategories = categoryRepository.findAllCategories();
        
        return buildCategoryTreeFromList(allCategories);
    }
    
    private List<CategoryResponse> buildCategoryTreeFromList(List<Category> allCategories) {
        Map<Long, List<Category>> categoriesByParentId = allCategories.stream()
                .filter(category -> category.getParent() != null)
                .collect(Collectors.groupingBy(category -> category.getParent().getId()));
        
        List<Category> rootCategories = allCategories.stream()
                .filter(category -> category.getParent() == null)
                .toList();
        
        return rootCategories.stream()
                .map(root -> buildCategoryTree(root, categoriesByParentId))
                .toList();
    }
    
    private CategoryResponse buildCategoryTree(Category category, Map<Long, List<Category>> categoriesByParentId) {
        CategoryResponse response = categoryMapper.toResponse(category);
        
        List<Category> children = categoriesByParentId.getOrDefault(category.getId(), List.of());
        
        List<CategoryResponse> childrenResponses = children.stream()
                .map(child -> buildCategoryTree(child, categoriesByParentId))
                .toList();
        
        response.setChildren(childrenResponses);
        return response;
    }
}
