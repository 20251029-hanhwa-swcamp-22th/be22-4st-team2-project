package com.salesboost.domain.admin.service;

import com.salesboost.domain.admin.dto.AdminLoginRequest;
import com.salesboost.domain.admin.dto.AdminLoginResponse;
import com.salesboost.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    public AdminLoginResponse login(AdminLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        String accessToken = jwtProvider.generateToken(authentication.getName());
        return new AdminLoginResponse(accessToken, "Bearer");

    }

}
