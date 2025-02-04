package com.example.coupang.search.service;

import com.example.coupang.search.entity.SearchKeyword;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class SearchKeywordGenerator {

    private final Faker faker = new Faker();

    /**
     * 랜덤한 검색어 데이터를 생성하는 메서드
     */
    public SearchKeyword generateRandomKeyword() {
        SearchKeyword keyword = new SearchKeyword();
        keyword.setId(UUID.randomUUID().toString());
        keyword.setSearchIc(faker.internet().uuid());
        keyword.setSearchText(faker.lorem().word());
        keyword.setCount(faker.number().numberBetween(1, 1000));
        keyword.setTimestamp(LocalDateTime.now());
        return keyword;
    }
}
