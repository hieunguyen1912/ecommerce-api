package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.CreateShopRequest;
import com.hieu.ecommerce.model.dto.request.ShopUpdateRequestDTO;
import com.hieu.ecommerce.model.dto.response.ShopDetailResponseDTO;
import com.hieu.ecommerce.model.dto.response.ShopListResponseDTO;
import com.hieu.ecommerce.model.dto.response.ShopResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ShopService {

    ShopResponseDTO createShop(CreateShopRequest request);

    List<ShopListResponseDTO> getAllShops(Pageable pageable);

    ShopDetailResponseDTO getShopById(Long id);

    ShopResponseDTO updateShop(Long id, ShopUpdateRequestDTO shopUpdateRequestDTO);

    void deleteShop(Long id);

    Long count();
}
