package com.hieu.ecommerce.controller.admin;

import com.hieu.ecommerce.common.annotation.ResponseMessage;
import com.hieu.ecommerce.common.constant.ProductStatus;
import com.hieu.ecommerce.model.dto.response.PageResponse;
import com.hieu.ecommerce.model.dto.response.ProductResponse;
import com.hieu.ecommerce.model.dto.response.ProductSummaryResponse;
import com.hieu.ecommerce.service.ProductService;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductSummaryResponse>> getAllProducts(
            @PageableDefault(page = 0, size = 10, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        List<ProductSummaryResponse> products = productService.getAllProductsForAdmin(pageable);
        return ResponseEntity
                .ok(new PageResponse<>(
                        products,
                        pageable.getPageNumber() + 1,
                        pageable.getPageSize(),
                        productService.countProducts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    @ResponseMessage("Delete product successfully")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProductForAdmin(id);
        return ResponseEntity.ok().build();
    }


    @PutMapping("/{id}/status")
    @ResponseMessage("Change product status successfully")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id,
                                              @RequestParam ProductStatus status) {
        productService.changeStatus(id, status);
        return ResponseEntity.ok().build();
    }
}
