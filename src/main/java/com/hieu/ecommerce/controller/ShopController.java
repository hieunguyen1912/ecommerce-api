package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.common.anotation.ResponseMessage;
import com.hieu.ecommerce.model.dto.request.CreateShopRequest;
import com.hieu.ecommerce.model.dto.request.ShopUpdateRequestDTO;
import com.hieu.ecommerce.model.dto.response.PageResponse;
import com.hieu.ecommerce.model.dto.response.ShopDetailResponseDTO;
import com.hieu.ecommerce.model.dto.response.ShopListResponseDTO;
import com.hieu.ecommerce.model.dto.response.ShopResponseDTO;
import com.hieu.ecommerce.service.ShopService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shops")
public class ShopController {
    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping
    @ResponseMessage("Create shop successfully")
    public ResponseEntity<ShopResponseDTO> createShop(@Valid @RequestBody CreateShopRequest request) {
        ShopResponseDTO response = shopService.createShop(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(response);
    }

    @GetMapping
    @ResponseMessage("Retrieved shop successfully")
    public ResponseEntity<PageResponse<ShopListResponseDTO>> getAllShops(Pageable pageable) {
        List<ShopListResponseDTO> shop = shopService.getAllShops(pageable);
        return ResponseEntity.ok(
                new PageResponse<>(
                        shop,
                        pageable.getPageNumber() + 1,
                        pageable.getPageSize(),
                        shopService.count()
                )
        );
    }

    @GetMapping("/{id}")
    @ResponseMessage("Retrieved shop successfully")
    public ResponseEntity<ShopDetailResponseDTO> getShopByid(@PathVariable Long id) {
        return ResponseEntity.ok(shopService.getShopById(id));
    }

    @PutMapping("/{id}")
    @ResponseMessage("Update shop successfully")
    public ResponseEntity<ShopResponseDTO> updateShop
            (@PathVariable Long id,
             @Valid @RequestBody ShopUpdateRequestDTO shopUpdateRequestDTO) {
        ShopResponseDTO shopResponseDTO = shopService.updateShop(id, shopUpdateRequestDTO);
        return ResponseEntity.ok(shopResponseDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseMessage("Delete shop successfully")
    public ResponseEntity<Void> deleteShop(@PathVariable Long id) {
        shopService.deleteShop(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
