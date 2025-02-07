package com.example.coupang.user.service;

import com.example.coupang.user.entity.User;
import com.example.coupang.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(String email, String rawPassword) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already in use: " + email);
        }

        // 이메일에서 @ 이전의 값 추출
        String name = email.split("@")[0];

        // 사용자 생성 및 저장
        User user = User.builder()
                .name(name) // 이메일의 @ 앞 부분을 이름으로 사용
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .build();

        return userRepository.save(user);
    }
}
