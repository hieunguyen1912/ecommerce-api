package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.model.dto.response.ProductResponse;
import com.hieu.ecommerce.model.dto.response.ProductSummaryResponse;
import com.hieu.ecommerce.service.ProductService;
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
    public ResponseEntity<List<ProductSummaryResponse>> getAllProducts(
            Pageable pageable
    ) {
        List<ProductSummaryResponse> products = productService.getAllProductsForUser(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getActiveProductById(id));
    }

}
