package com.hieu.ecommerce.common.constant;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum VariantStatus {
    @JsonProperty("active")
    ACTIVE,
    @JsonProperty("inactive")
    INACTIVE,
    @JsonProperty("out_of_stock")
    OUT_OF_STOCK,
    @JsonProperty("deleted")
    DELETED
}