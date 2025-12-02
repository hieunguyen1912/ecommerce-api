package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.annotation.ResponseMessage;
import com.hieu.ecommerce.model.dto.request.CreateCategoryRequest;
import com.hieu.ecommerce.model.dto.request.UpdateCategoryRequest;
import com.hieu.ecommerce.model.dto.response.CategoryResponse;
import com.hieu.ecommerce.model.dto.response.PageResponse;
import com.hieu.ecommerce.service.CategoryService;
import com.hieu.ecommerce.util.PaginationHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "API endpoints for category management (Admin only)")
public class CategoryManagementController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories (Admin)", description = "Retrieve paginated list of all categories. Requires ADMIN role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Categories retrieved successfully")
    public ResponseEntity<PageResponse<CategoryResponse>> getAllCategories(Pageable pageable) {
        Page<CategoryResponse> categories = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(PaginationHelper.toPaginatedResponse(categories));
    }

    @GetMapping("/tree")
    @Operation(summary = "Get category tree (Admin)", description = "Retrieve hierarchical category tree. Requires ADMIN role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category tree retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Category tree retrieved successfully")
    public ResponseEntity<List<CategoryResponse>> getCategoryTree() {
        return ResponseEntity.ok(categoryService.getCategoryTree());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID (Admin)", description = "Retrieve category details by ID. Requires ADMIN role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Category retrieved successfully")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PostMapping
    @Operation(summary = "Create category", description = "Create a new category. Requires ADMIN role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Category created successfully")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest createCategoryRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(createCategoryRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Update category information. Requires ADMIN role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Category updated successfully")
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest updateCategoryRequest) {
        return ResponseEntity.ok(categoryService.updateCategory(id, updateCategoryRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Delete a category. Requires ADMIN role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Category deleted successfully")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}

