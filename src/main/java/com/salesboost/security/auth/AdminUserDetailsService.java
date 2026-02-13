package com.salesboost.security.auth;

import com.salesboost.domain.admin.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return adminUserRepository.findByUsername(username)
                .map(AdminUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("관리자 계정을 찾을 수 없습니다."));
    }
}
