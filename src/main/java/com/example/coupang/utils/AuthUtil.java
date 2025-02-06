package com.example.coupang.utils;

import com.example.coupang.security.UserDetailsImpl;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * 인증된 사용자 정보를 제공하는 유틸리티 클래스.
 */
@UtilityClass // 유틸리티 클래스로 선언
public class AuthUtil {

    /**
     * 현재 인증된 사용자의 ID를 반환합니다.
     *
     * @return 사용자 ID (인증되지 않은 경우 null)
     */
    public Long getId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }

        return null; // 인증되지 않은 경우
    }

}
