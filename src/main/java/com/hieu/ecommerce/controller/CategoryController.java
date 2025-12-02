package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.annotation.ResponseMessage;
import com.hieu.ecommerce.model.dto.response.CategoryResponse;
import com.hieu.ecommerce.model.dto.response.PageResponse;
import com.hieu.ecommerce.service.CategoryService;
import com.hieu.ecommerce.util.PaginationHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "API endpoints for category browsing")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieve paginated list of all categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    @ResponseMessage("Categories retrieved successfully")
    public ResponseEntity<PageResponse<CategoryResponse>> getAllCategories(Pageable pageable) {
        Page<CategoryResponse> categories = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(PaginationHelper.toPaginatedResponse(categories));
    }

    @GetMapping("/tree")
    @Operation(summary = "Get category tree", description = "Retrieve hierarchical category tree structure")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category tree retrieved successfully")
    })
    @ResponseMessage("Category tree retrieved successfully")
    public ResponseEntity<List<CategoryResponse>> getCategoryTree() {
        List<CategoryResponse> categoryTree = categoryService.getCategoryTree();
        return ResponseEntity.ok(categoryTree);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieve category details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @ResponseMessage("Category retrieved successfully")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }
}

