package com.hieu.ecommerce.controller.shop;

import com.hieu.ecommerce.common.annotation.ResponseMessage;
import com.hieu.ecommerce.common.constant.ProductStatus;
import com.hieu.ecommerce.facade.ProductFacade;
import com.hieu.ecommerce.model.dto.request.CreateProductRequest;
import com.hieu.ecommerce.model.dto.request.UpdateProductRequest;
import com.hieu.ecommerce.model.dto.response.PageResponse;
import com.hieu.ecommerce.model.dto.response.ProductResponse;
import com.hieu.ecommerce.model.dto.response.ProductSummaryResponse;
import com.hieu.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/shop/products")
public class ShopProductController {
    private final ProductService productService;
    private final ProductFacade productFacade;

    public ShopProductController(ProductService productService, ProductFacade productFacade) {
        this.productService = productService;
        this.productFacade = productFacade;
    }

    @PostMapping
    @ResponseMessage("Create product successfully")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse product = productFacade.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{id}")
    @ResponseMessage("Update product successfully")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductResponse updatedProduct = productFacade.updateProduct(id, request);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @ResponseMessage("Delete product successfully")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProductForShop(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/status")
    @ResponseMessage("Change product status successfully")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id,
                                             @RequestParam ProductStatus status) {
        productService.changeStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @ResponseMessage("Get all products successfully")
    public ResponseEntity<PageResponse<ProductSummaryResponse>> getProducts(
            @PageableDefault(page = 0, size = 10, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        List<ProductSummaryResponse> products = productService.getAllProductsByShopId(pageable);
        return ResponseEntity
                .ok(new PageResponse<>(
                        products,
                        pageable.getPageNumber() + 1,
                        pageable.getPageSize(),
                        productService.countProducts()));
    }

    @GetMapping("/{id}")
    @ResponseMessage("Get product by ID successfully")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductByIdAndShopId(id);
        return ResponseEntity.ok(product);
    }
}
