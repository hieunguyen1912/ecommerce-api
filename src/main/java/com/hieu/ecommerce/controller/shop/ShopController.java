package com.hieu.ecommerce.controller.shop;

import com.hieu.ecommerce.common.annotation.ResponseMessage;
import com.hieu.ecommerce.model.dto.request.CreateShopRequest;
import com.hieu.ecommerce.model.dto.request.ShopUpdateRequestDTO;
import com.hieu.ecommerce.model.dto.response.ShopDetailResponseDTO;
import com.hieu.ecommerce.model.dto.response.ShopResponseDTO;
import com.hieu.ecommerce.service.ShopService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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

    @GetMapping()
    @ResponseMessage("Retrieved shop successfully")
    public ResponseEntity<ShopDetailResponseDTO> getShopProfile() {
        return ResponseEntity.ok(shopService.getShopProfile());
    }

    @PutMapping()
    @ResponseMessage("Update shop successfully")
    public ResponseEntity<ShopResponseDTO> updateShop
            (@Valid @RequestBody ShopUpdateRequestDTO shopUpdateRequestDTO) {
        ShopResponseDTO shopResponseDTO = shopService.updateShop(shopUpdateRequestDTO);
        return ResponseEntity.ok(shopResponseDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseMessage("Delete shop successfully")
    public ResponseEntity<Void> deleteShop(@PathVariable Long id) {
        shopService.deleteShop(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }


}