package com.salesboost.domain.admin.service;

import com.salesboost.common.exception.BusinessException;
import com.salesboost.common.exception.ErrorCode;
import com.salesboost.domain.admin.dto.AdminLoginRequest;
import com.salesboost.domain.admin.dto.AdminLoginResponse;
import com.salesboost.domain.admin.dto.TokenRefreshRequest;
import com.salesboost.domain.admin.entity.AdminUser;
import com.salesboost.domain.admin.entity.RefreshToken;
import com.salesboost.domain.admin.repository.AdminUserRepository;
import com.salesboost.domain.admin.repository.RefreshTokenRepository;
import com.salesboost.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final AdminUserRepository adminUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-expiration-days:7}")
    private long refreshExpirationDays;

    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        AdminUser adminUser = adminUserRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

        // 만료된 토큰만 정리 (동시 접속 허용)
        refreshTokenRepository.deleteByAdminUserIdAndExpiresAtBefore(
                adminUser.getId(), java.time.LocalDateTime.now());

        String accessToken = jwtProvider.generateToken(authentication.getName());
        RefreshToken refreshToken = RefreshToken.create(adminUser, refreshExpirationDays);
        refreshTokenRepository.save(refreshToken);

        return new AdminLoginResponse(accessToken, refreshToken.getToken(), "Bearer");
    }

    @Transactional
    public AdminLoginResponse refreshToken(TokenRefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        AdminUser adminUser = refreshToken.getAdminUser();

        // 사용된 토큰만 삭제 (토큰 로테이션 — 다른 기기 세션 유지)
        refreshTokenRepository.delete(refreshToken);

        String newAccessToken = jwtProvider.generateToken(adminUser.getUsername());
        RefreshToken newRefreshToken = RefreshToken.create(adminUser, refreshExpirationDays);
        refreshTokenRepository.save(newRefreshToken);

        return new AdminLoginResponse(newAccessToken, newRefreshToken.getToken(), "Bearer");
    }

    @Transactional
    public void logout(String username) {
        adminUserRepository.findByUsername(username)
                .ifPresent(user -> refreshTokenRepository.deleteByAdminUserId(user.getId()));
    }
}
