package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.OrderFilterRequest;
import org.springframework.core.io.Resource;

public interface OrderExportService {
    Resource exportOrdersToCsv(OrderFilterRequest filter);
}

