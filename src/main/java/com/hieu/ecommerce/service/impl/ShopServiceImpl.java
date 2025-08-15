package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.common.SecurityUtil;
import com.hieu.ecommerce.mapper.ShopMapper;
import com.hieu.ecommerce.model.dto.request.CreateShopRequest;
import com.hieu.ecommerce.model.dto.response.ShopResponse;
import com.hieu.ecommerce.model.entity.Shop;
import com.hieu.ecommerce.model.entity.User;
import com.hieu.ecommerce.repository.ShopRepository;
import com.hieu.ecommerce.repository.UserRepository;
import com.hieu.ecommerce.service.ShopService;
import org.springframework.stereotype.Service;

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
    public ShopResponse createShop(CreateShopRequest request) {
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
        return shopMapper.toResponsee(shop);
    }


}
