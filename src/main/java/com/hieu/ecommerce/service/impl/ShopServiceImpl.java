package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.common.SecurityUtil;
import com.hieu.ecommerce.common.constant.ProductStatus;
import com.hieu.ecommerce.common.constant.ShopStatus;
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
import com.hieu.ecommerce.repository.ProductRepository;
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
    private final ProductRepository productRepository;

    public ShopServiceImpl(ShopMapper shopMapper, ShopRepository shopRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.shopMapper = shopMapper;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    public ShopResponseDTO createShop(CreateShopRequest request) {

        User user = getCurrentUser();

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
    public List<ShopListResponseDTO> getAllShopsForUser(Pageable pageable) {
        return shopRepository.findAllByStatus(ShopStatus.ACTIVE, pageable)
                .stream()
                .map(shopMapper::toShopListResponse).toList();
    }

    @Override
    public List<ShopListResponseDTO> getAllShopsForAdmin(Pageable pageable) {
        return shopRepository.findAll(pageable)
                .stream()
                .map(shopMapper::toShopListResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ShopDetailResponseDTO getShopProfile() {

        return shopMapper.toshopDetailResponseDTO(getCurrentUserShop());
    }

    @Override
    public ShopDetailResponseDTO getShopById(Long id) {

        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        return shopMapper.toshopDetailResponseDTO(shop);
    }

    @Override
    public ShopResponseDTO updateShop(ShopUpdateRequestDTO requestDTO) {

        User currentUser = getCurrentUser();

        Long shopId = currentUser.getShop().getId();

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        if (!requestDTO.getEmail().equals(shop.getEmail()) && shopRepository.existsByEmail(requestDTO.getEmail())) {
            throw new IllegalArgumentException("Shop with this email already exists");
        }

        if (shopRepository.existsByShopNameAndIdNot(requestDTO.getShopName(), shopId)) {
            throw new IllegalArgumentException("Shop with this name already exists");
        }

        if (shopRepository.existsByPhoneNumberAndIdNot(requestDTO.getPhoneNumber(), shopId)) {
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
    public void changeStatus(Long shopId, ShopStatus shopStatus) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        if (shop.getStatus() == ShopStatus.DELETED) {
            throw new IllegalArgumentException("Cannot change status of an deleted shop");
        }

        if (shopStatus == ShopStatus.ACTIVE && shop.getStatus() == ShopStatus.ACTIVE) {
            throw new IllegalArgumentException("Shop is already active");
        }

        if (shopStatus == ShopStatus.INACTIVE && shop.getStatus() == ShopStatus.INACTIVE) {
            throw new IllegalArgumentException("Shop is already inactive");
        }

        shop.setStatus(shopStatus);
        shopRepository.save(shop);

        if (shopStatus == ShopStatus.INACTIVE) {
            productRepository.updateStatusByShop(shopId, ProductStatus.INACTIVE);
        } else if (shopStatus == ShopStatus.ACTIVE) {
            productRepository.updateStatusByShop(shopId, ProductStatus.ACTIVE);
        }
    }

    @Override
    @Transactional
    public void deleteShop(Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        shop.setStatus(ShopStatus.DELETED);

        List<Product> products = shop.getProducts();
        for (Product product : products) {
            product.setStatus(ProductStatus.DELETED);
        }
        shopRepository.save(shop);
    }

    public void deleteShopByOwner() {
        Shop shop = getCurrentUserShop();

        shop.setStatus(ShopStatus.DELETED);

        List<Product> products = shop.getProducts();
        for (Product product : products) {
            product.setStatus(ProductStatus.DELETED);
        }
        shopRepository.save(shop);
    }

    @Override
    public Long count() {
        return shopRepository.count();
    }

    @Override
    public Shop getCurrentUserShop() {
        Long userId = SecurityUtil.getCurrentUserId();
        return shopRepository.findByUserIdAndStatus(userId, ShopStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for user ID: " + userId));
    }

    private User getCurrentUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for user ID: " + userId));
    }
}

