package com.hieu.ecommerce.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class LoginResponse {
    @JsonProperty("access_token")
    private String accessToken;
    private String refreshToken;
    private UserResponse user;
}
