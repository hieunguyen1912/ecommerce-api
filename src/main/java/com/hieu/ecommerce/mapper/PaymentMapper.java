package com.hieu.ecommerce.mapper;

import com.hieu.ecommerce.model.dto.response.PaymentResponse;
import com.hieu.ecommerce.model.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.orderNumber", target = "orderNumber")
    PaymentResponse toResponse(Payment payment);
}
