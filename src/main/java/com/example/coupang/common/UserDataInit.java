package com.example.coupang.common;

import com.example.coupang.user.entity.User;
import com.example.coupang.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDataInit implements CommandLineRunner {

    private final UserRepository userRepository;


    @Override
    public void run(String... args) throws Exception {
        // 더미 유저가 없다면 유저 데이터를 추가
        if (userRepository.count() == 0) {
            // 더미 유저 200명 생성
            for (int i = 1; i <= 200; i++) {
                User user = new User("User" + i, "user" + i + "@example.com", "password" + i);
                userRepository.save(user); // 유저 저장
            }
            log.info("유저 더미 데이터가 성공적으로 생성되었습니다.");
        }
    }
}
