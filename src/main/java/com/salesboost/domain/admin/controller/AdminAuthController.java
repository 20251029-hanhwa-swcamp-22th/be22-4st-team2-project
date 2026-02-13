package com.salesboost.domain.admin.controller;

import com.salesboost.common.response.ApiResponse;
import com.salesboost.domain.admin.dto.AdminLoginRequest;
import com.salesboost.domain.admin.dto.AdminLoginResponse;
import com.salesboost.domain.admin.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        return ApiResponse.ok(adminAuthService.login(request));
    }
}
