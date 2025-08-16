package com.hieu.ecommerce.common.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ProductStatus {
    @JsonProperty("active")
    ACTIVE,
    @JsonProperty("inactive")
    INACTIVE,
    @JsonProperty("suspended")
    SUSPENDED
}
