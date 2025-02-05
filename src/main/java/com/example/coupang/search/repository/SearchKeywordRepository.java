package com.example.coupang.search.repository;

// SearchKeywordRepository.java
import com.example.coupang.search.entity.SearchKeyword;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SearchKeywordRepository extends ElasticsearchRepository<SearchKeyword, String> {

    // 특정 검색어 조회
    SearchKeyword findBySearchText(String searchText);

    // 인기 검색어 상위 10개 조회
    List<SearchKeyword> findTop10ByOrderByCountDesc();
}