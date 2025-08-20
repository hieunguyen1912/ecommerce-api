package com.hieu.ecommerce.controller.admin;

import com.hieu.ecommerce.common.annotation.ResponseMessage;
import com.hieu.ecommerce.common.constant.ShopStatus;
import com.hieu.ecommerce.model.dto.response.PageResponse;
import com.hieu.ecommerce.model.dto.response.ShopDetailResponseDTO;
import com.hieu.ecommerce.model.dto.response.ShopListResponseDTO;
import com.hieu.ecommerce.service.ShopService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/shops")
public class AdminShopController {
    private final ShopService shopService;

    public AdminShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping
    @ResponseMessage("Retrieved shop successfully")
    public ResponseEntity<PageResponse<ShopListResponseDTO>> getAllShops(Pageable pageable) {
        List<ShopListResponseDTO> shop = shopService.getAllShopsForAdmin(pageable);
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

    @DeleteMapping("/{id}")
    @ResponseMessage("Delete shop successfully")
    public ResponseEntity<Void> deleteShop(@PathVariable Long id) {
        shopService.deleteShop(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PutMapping("/{id}/suspend")
    public ResponseEntity<Void> suspendShop(@PathVariable Long id) {
        shopService.changeStatus(id, ShopStatus.INACTIVE);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/unsuspend")
    public ResponseEntity<Void> unsuspendShop(@PathVariable Long id) {
        shopService.changeStatus(id, ShopStatus.ACTIVE);
        return ResponseEntity.noContent().build();
    }

}
