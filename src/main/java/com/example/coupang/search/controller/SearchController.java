package com.example.coupang.search.controller;

// SearchService.java
import com.example.coupang.search.entity.SearchKeyword;
import com.example.coupang.search.repository.SearchKeywordRepository;
import com.example.coupang.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.example.coupang.search.service.BulkInsertService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;
    private final BulkInsertService bulkInsertService;

    // 검색어 저장 API
    @PostMapping
    public SearchKeyword saveSearchKeyword(@RequestBody Map<String, Object> request) {
        String searchIc = (String) request.get("search_ic");
        String searchText = (String) request.get("search_text");

        return searchService.saveOrUpdateSearchKeyword(searchIc, searchText);
    }

    // ✅ 기존 인기 검색어 조회 API
    @GetMapping("/popular")
    public List<String> getPopularKeywords() throws IOException {
        return searchService.getPopularKeywords();
    }

    // ✅ 최적화 1 (executionHint 사용)
    @GetMapping("/popular/optimized")
    public List<String> getPopularKeywordsOptimized() throws IOException {
        return searchService.getPopularKeywordsOptimized();
    }

    // ✅ 최적화 2 (Cache & Filter 사용)
    @GetMapping("/popular/fastest")
    public List<String> getPopularKeywordsFastest() throws IOException {
        return searchService.getPopularKeywordsFastest();
    }

//    @GetMapping("/autocomplete")
//    public List<String> getAutocomplete(@RequestParam String query) throws IOException {
//        return searchService.getSuggestions(query);
//    }

    @PostMapping("/insert/{count}")
    public String insertBulkData(@PathVariable int count) {
        bulkInsertService.insertBulkData(count);
        return count + "개의 검색어 데이터를 Elasticsearch에 추가했습니다.";
    }

}