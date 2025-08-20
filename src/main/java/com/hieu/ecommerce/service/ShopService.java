package com.hieu.ecommerce.service;

import com.hieu.ecommerce.common.constant.ShopStatus;
import com.hieu.ecommerce.model.dto.request.CreateShopRequest;
import com.hieu.ecommerce.model.dto.request.ShopUpdateRequestDTO;
import com.hieu.ecommerce.model.dto.response.ShopDetailResponseDTO;
import com.hieu.ecommerce.model.dto.response.ShopListResponseDTO;
import com.hieu.ecommerce.model.dto.response.ShopResponseDTO;
import com.hieu.ecommerce.model.entity.Shop;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ShopService {

    ShopResponseDTO createShop(CreateShopRequest request);

    List<ShopListResponseDTO> getAllShopsForUser(Pageable pageable);

    List<ShopListResponseDTO> getAllShopsForAdmin(Pageable pageable);

    ShopDetailResponseDTO getShopById(Long id);

    ShopDetailResponseDTO getShopProfile();

    ShopResponseDTO updateShop(ShopUpdateRequestDTO shopUpdateRequestDTO);

    Shop getCurrentUserShop();

    void changeStatus(Long shopId, ShopStatus shopStatus);

    void deleteShop(Long id);

    Long count();
}
