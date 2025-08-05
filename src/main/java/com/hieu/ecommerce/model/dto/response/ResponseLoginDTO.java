package com.hieu.ecommerce.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResponseLoginDTO {
    @JsonProperty("access_token")
    private String accessToken;
    private UserInfo userInfo;
}
