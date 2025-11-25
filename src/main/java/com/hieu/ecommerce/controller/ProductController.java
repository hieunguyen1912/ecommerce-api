package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.constant.ProductStatus;
import com.hieu.ecommerce.model.dto.request.ProductFilterRequest;
import com.hieu.ecommerce.model.dto.response.PageResponse;
import com.hieu.ecommerce.model.dto.response.ProductResponse;
import com.hieu.ecommerce.model.dto.response.ProductSummaryResponse;
import com.hieu.ecommerce.service.ProductService;
import com.hieu.ecommerce.util.PaginationHelper;
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
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductSummaryResponse>> getAllProducts(
            Pageable pageable
    ) {
        Page<ProductSummaryResponse> products = productService.getAllProducts(pageable,
                ProductFilterRequest.builder().statuses(List.of(ProductStatus.ACTIVE)).build());
        return ResponseEntity.ok(PaginationHelper.toPaginatedResponse(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getActiveProductById(id));
    }

}
