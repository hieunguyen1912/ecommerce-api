package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.CreateShopRequest;
import com.hieu.ecommerce.model.dto.response.ShopResponse;

public interface ShopService {

    ShopResponse createShop(CreateShopRequest request);
}
