package com.hieu.ecommerce.model.dto.response;

import lombok.*;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfo {
    private Long id;
    private String fullName;
    private String email;
    //private Set<Role> roles;
}