package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.common.SecurityUtil;
import com.hieu.ecommerce.common.enums.ProductStatus;
import com.hieu.ecommerce.common.enums.ShopStatus;
import com.hieu.ecommerce.exception.ResourceNotFoundException;
import com.hieu.ecommerce.mapper.ShopMapper;
import com.hieu.ecommerce.model.dto.request.CreateShopRequest;
import com.hieu.ecommerce.model.dto.request.ShopUpdateRequestDTO;
import com.hieu.ecommerce.model.dto.response.ShopDetailResponseDTO;
import com.hieu.ecommerce.model.dto.response.ShopListResponseDTO;
import com.hieu.ecommerce.model.dto.response.ShopResponseDTO;
import com.hieu.ecommerce.model.entity.Product;
import com.hieu.ecommerce.model.entity.Shop;
import com.hieu.ecommerce.model.entity.User;
import com.hieu.ecommerce.repository.ShopRepository;
import com.hieu.ecommerce.repository.UserRepository;
import com.hieu.ecommerce.service.ShopService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShopServiceImpl implements ShopService {
    private final ShopMapper shopMapper;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    public ShopServiceImpl(ShopMapper shopMapper, ShopRepository shopRepository, UserRepository userRepository) {
        this.shopMapper = shopMapper;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ShopResponseDTO createShop(CreateShopRequest request) {
        String userName = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IllegalArgumentException("User not authenticated"));

        System.out.println("Creating shop for user ID: " + userName);

        User user = userRepository.findByEmail(userName)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (shopRepository.existsByUserId(user.getId())) {
            throw new IllegalArgumentException("User already has a shop");
        }

        if (shopRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Shop with this email already exists");
        }
        if (shopRepository.existsByShopName(request.getShopName())) {
            throw new IllegalArgumentException("Shop with this name already exists");
        }
        if (shopRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Shop with this phone number already exists");
        }

        Shop shop = shopMapper.toEntity(request);
        shop.setUser(user);
        shop = shopRepository.save(shop);
        return shopMapper.toShopResponseDTO(shop);
    }

    @Override
    public List<ShopListResponseDTO> getAllShops(Pageable pageable) {
        return shopRepository.findAllByStatus(ShopStatus.ACTIVE, pageable)
                .stream()
                .map(shopMapper::toShopListResponse).toList();
    }

    @Override
    public ShopDetailResponseDTO getShopById(Long id) {
        Shop shop = shopRepository.findByIdAndStatus(id, ShopStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        return shopMapper.toshopDetailResponseDTO(shop);
    }

    @Override
    public ShopResponseDTO updateShop(Long id, ShopUpdateRequestDTO requestDTO) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        if (!requestDTO.getEmail().equals(shop.getEmail()) && shopRepository.existsByEmail(requestDTO.getEmail())) {
            throw new IllegalArgumentException("Shop with this email already exists");
        }

        if (shopRepository.existsByShopNameAndIdNot(requestDTO.getShopName(), id)) {
            throw new IllegalArgumentException("Shop with this name already exists");
        }

        if (shopRepository.existsByPhoneNumberAndIdNot(requestDTO.getPhoneNumber(), id)) {
            throw new IllegalArgumentException("Shop with this phone number already exists");
        }

        if (requestDTO.getEmail() != null) {
            shop.setEmail(requestDTO.getEmail());
        }

        if (requestDTO.getShopName() != null) {
            shop.setShopName(requestDTO.getShopName());
        }
        if (requestDTO.getDescription() != null) {
            shop.setDescription(requestDTO.getDescription());
        }
        if (requestDTO.getAddress() != null) {
            shop.setAddress(requestDTO.getAddress());
        }
        if (requestDTO.getPhoneNumber() != null) {
            shop.setPhoneNumber(requestDTO.getPhoneNumber());
        }
        if (requestDTO.getLogoUrl() != null) {
            shop.setLogoUrl(requestDTO.getLogoUrl());
        }

        Shop updatedShop = shopRepository.save(shop);

        return shopMapper.toShopResponseDTO(updatedShop);
    }

    @Override
    @Transactional
    public void deleteShop(Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        shop.setStatus(ShopStatus.INACTIVE);

        List<Product> products = shop.getProducts();
        for (Product product : products) {
            product.setStatus(ProductStatus.INACTIVE);
        }
        shopRepository.save(shop);
    }

    @Override
    public Long count() {
        return shopRepository.count();
    }


}
