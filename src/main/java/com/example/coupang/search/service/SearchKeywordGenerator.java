package com.example.coupang.search.service;

import com.example.coupang.search.entity.SearchKeyword;
import net.datafaker.Faker;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class SearchKeywordGenerator {

    private final Faker faker = new Faker();

    /**
     * 랜덤한 검색어 데이터를 생성하는 메서드
     */
    public SearchKeyword generateRandomKeyword() {
        String randomWord = faker.lorem().word();  // 랜덤 단어 생성
        List<String> suggestions = List.of(
                randomWord,
                randomWord + "file",
                randomWord + "name",
                randomWord + "data"
        ); // 유사어 자동 생성

        SearchKeyword keyword = new SearchKeyword();
        keyword.setId(UUID.randomUUID().toString());
        keyword.setSearchIc(faker.internet().uuid());
        keyword.setSearchText(randomWord);
        keyword.setCount(faker.number().numberBetween(1, 1000));
        keyword.setTimestamp(LocalDateTime.now());

        // ✅ Completion Suggest 설정 (contexts 필드 포함)
        Completion completion = new Completion();
        completion.setInput(suggestions.toArray(new String[0]));

        // ✅ 필수 필드인 `contexts`에 기본값 설정
        completion.setContexts(Map.of("category", List.of("default_category")));

        // ✅ weight 값도 기본값 설정
        completion.setWeight(faker.number().numberBetween(1, 100));

        keyword.setSuggest(completion);

        return keyword;
    }
}
