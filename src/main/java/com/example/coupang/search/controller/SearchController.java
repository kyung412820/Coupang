package com.example.coupang.search.controller;

// SearchService.java
import com.example.coupang.search.entity.SearchKeyword;
import com.example.coupang.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.coupang.search.service.BulkInsertService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;
    private final BulkInsertService bulkInsertService;


    // 🔹 1. 검색어 저장 API
    @PostMapping
    public SearchKeyword saveSearchKeyword(@RequestBody Map<String, Object> request) {
        String searchIc = (String) request.get("search_ic");
        String searchText = (String) request.get("search_text");

        return searchService.saveOrUpdateSearchKeyword(searchIc, searchText);
    }


    // 🔹 2. 인기 검색어 조회 (기본)
    @GetMapping("/popular")
    public List<String> getPopularKeywords() throws IOException {
        return searchService.getPopularKeywords();
    }

    // 🔹 3. 인기 검색어 조회 (최적화)
    @GetMapping("/popular/optimized")
    public List<String> getPopularKeywordsOptimized() throws IOException {
        return searchService.getPopularKeywordsOptimized();
    }

    // 🔹 4. 인기 검색어 조회 (캐싱 활성화)
    @GetMapping("/popular/fastest")
    public List<String> getPopularKeywordsFastest() throws IOException {
        return searchService.getPopularKeywordsFastest();
    }

    // 🔹 5. 자동완성 추천 검색어 (Completion Suggester)
//    @GetMapping("/suggestions")
//    public List<String> getSuggestions(@RequestParam String query) throws IOException {
//        return searchService.getSuggestions(query);
//    }

    // 🔹 6. 검색어 검색 (SearchKeyword 기반)
    @GetMapping("/keywords")
    public List<SearchKeyword> searchKeywords(@RequestParam String keyword) {
        return searchService.searchKeywords(keyword);
    }

    @PostMapping("/insert/{count}")
    public String insertBulkData(@PathVariable int count) {
        bulkInsertService.insertBulkData(count);
        return count + "개의 검색어 데이터를 Elasticsearch에 추가했습니다.";
    }

    /**
     * 🔹 1. 전체 데이터 삭제 (인덱스 유지)
     */
    @DeleteMapping("/clear")
    public ResponseEntity<String> deleteAllDocuments() {
        try {
            searchService.deleteAllDocuments();
            return ResponseEntity.ok("모든 문서가 삭제되었습니다.");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("문서 삭제 중 오류 발생: " + e.getMessage());
        }
    }
}

