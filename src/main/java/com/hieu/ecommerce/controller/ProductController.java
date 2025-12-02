package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.constant.ProductStatus;
import com.hieu.ecommerce.model.dto.request.ProductFilterRequest;
import com.hieu.ecommerce.model.dto.response.PageResponse;
import com.hieu.ecommerce.model.dto.response.ProductResponse;
import com.hieu.ecommerce.model.dto.response.ProductSummaryResponse;
import com.hieu.ecommerce.service.ProductService;
import com.hieu.ecommerce.util.PaginationHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "API endpoints for product browsing")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve paginated list of active products")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    public ResponseEntity<PageResponse<ProductSummaryResponse>> getAllProducts(
            Pageable pageable
    ) {
        Page<ProductSummaryResponse> products = productService.getAllProducts(pageable,
                ProductFilterRequest.builder().statuses(List.of(ProductStatus.ACTIVE)).build());
        return ResponseEntity.ok(PaginationHelper.toPaginatedResponse(products));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve product details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(productService.getActiveProductById(id));
    }

}
