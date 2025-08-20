package com.hieu.ecommerce.common.constant;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ShopStatus {
    @JsonProperty("active")
    ACTIVE,
    @JsonProperty("inactive")
    INACTIVE,
    @JsonProperty("deleted")
    DELETED
}
