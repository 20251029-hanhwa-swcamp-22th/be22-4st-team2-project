package com.salesboost.domain.admin.controller;

import com.salesboost.common.response.ApiResponse;
import com.salesboost.domain.admin.dto.AdminLoginRequest;
import com.salesboost.domain.admin.dto.AdminLoginResponse;
import com.salesboost.domain.admin.dto.TokenRefreshRequest;
import com.salesboost.domain.admin.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.ok(adminAuthService.login(request));
    }

    @PostMapping("/token/refresh")
    public ApiResponse<AdminLoginResponse> refreshToken(@RequestBody TokenRefreshRequest request) {
        return ApiResponse.ok(adminAuthService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal UserDetails userDetails) {
        adminAuthService.logout(userDetails.getUsername());
        return ApiResponse.ok("로그아웃 되었습니다.", null);
    }
}
