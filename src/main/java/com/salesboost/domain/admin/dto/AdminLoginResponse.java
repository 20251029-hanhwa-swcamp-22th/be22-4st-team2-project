package com.salesboost.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminLoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
}
