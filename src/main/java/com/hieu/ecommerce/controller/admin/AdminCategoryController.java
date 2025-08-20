package com.hieu.ecommerce.controller.admin;

import com.hieu.ecommerce.common.annotation.ResponseMessage;
import com.hieu.ecommerce.model.dto.request.CategoryRequest;
import com.hieu.ecommerce.model.dto.response.CategoryResponse;
import com.hieu.ecommerce.model.dto.response.PageResponse;
import com.hieu.ecommerce.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class AdminCategoryController {
    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ResponseMessage("Category created successfully")
    public ResponseEntity<CategoryResponse> createCategory(@Validated(CategoryRequest.OnCreate.class) @RequestBody CategoryRequest createCategoryRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(createCategoryRequest));
    }

    @GetMapping("/{id}")
    @ResponseMessage("Category retrieved successfully")
    public CategoryResponse getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    @PutMapping("/{id}")
    @ResponseMessage("Category updated successfully")
    public CategoryResponse updateCategory(@PathVariable Long id, @Validated(CategoryRequest.OnUpdate.class) @RequestBody CategoryRequest updateCategoryRequest) {
        return categoryService.updateCategory(id, updateCategoryRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseMessage("Category deleted successfully")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @ResponseMessage("Categories retrieved successfully")
    public ResponseEntity<PageResponse<CategoryResponse>> getAllCategories(
            Pageable pageable
    ) {
        List<CategoryResponse> category = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(new PageResponse<>(
                category,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                category.size()
        ));
    }
}
