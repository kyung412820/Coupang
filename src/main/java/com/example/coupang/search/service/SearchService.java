package com.example.coupang.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.query_dsl.DisMaxQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchPhraseQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.DeleteByQueryResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.*;
import com.example.coupang.search.entity.SearchKeyword;
import com.example.coupang.search.repository.SearchKeywordRepository;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregationExecutionHint;
import com.example.coupang.search.entity.BookDocument;
import com.example.coupang.search.dto.BookSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class SearchService {

    private final SearchKeywordRepository searchKeywordRepository;
    private final ElasticsearchClient client;
    private static final String INDEX_NAME = "search_keywords"; //

    // ---------------------------------------------------------------------
    // 1. 검색어 저장/업데이트 기능
    // ---------------------------------------------------------------------
    /**
     * 검색어를 저장하거나 업데이트합니다.
     * 이미 존재하면 검색 횟수를 증가시키고, 없으면 새로 생성합니다.
     */
    public SearchKeyword saveOrUpdateSearchKeyword(String searchIc, String searchText) {
        SearchKeyword keyword = searchKeywordRepository.findBySearchText(searchText);

        if (keyword != null) {
            keyword.setCount(keyword.getCount() + 1);
        } else {
            keyword = new SearchKeyword();
            keyword.setSearchIc(searchIc);
            keyword.setSearchText(searchText);
            keyword.setCount(1);
            keyword.setTimestamp(LocalDateTime.now());
        }

        return searchKeywordRepository.save(keyword);
    }

    // ---------------------------------------------------------------------
    // 2. 인기 검색어 조회 기능 (Aggregation 이용)
    // ---------------------------------------------------------------------
    /**
     * Elasticsearch Aggregation을 사용하여 인기 검색어(상위 10개)를 조회합니다.
     */
    public List<String> getPopularKeywords() throws IOException {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index("search_keywords")  // 검색어 인덱스 지정
                .aggregations("popular_searches", a -> a
                        .terms(t -> t
                                .field("searchText.keyword")  // ✅ keyword 필드 사용
                                .size(1000)))
                .build();

        // Elasticsearch 검색 실행
        SearchResponse<Void> response = client.search(searchRequest, Void.class);

        // Aggregation 결과에서 추천 검색어를 추출
        List<String> popularKeywords = new ArrayList<>();

        if (response.aggregations() != null && response.aggregations().containsKey("popular_searches")) {
            response.aggregations()
                    .get("popular_searches")
                    .sterms()
                    .buckets().array()
                    .forEach(bucket -> popularKeywords.add(bucket.key().stringValue()));
        } else {
            System.out.println("⚠️ Aggregation 결과가 없습니다!");
        }

        return popularKeywords;
    }



    public List<String> getPopularKeywordsOptimized() throws IOException {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index("search_keywords")
                .aggregations("popular_searches", a -> a
                        .terms(t -> t
                                .field("searchText.keyword")  // ✅ keyword 필드 사용
                                .size(1000)
                                .executionHint(TermsAggregationExecutionHint.Map))) // ✅ 실행 힌트 적용
                .build();

        // Elasticsearch 검색 실행
        SearchResponse<Void> response = client.search(searchRequest, Void.class);

        // Aggregation 결과에서 추천 검색어를 추출
        List<String> popularKeywords = new ArrayList<>();
        response.aggregations()
                .get("popular_searches")
                .sterms()
                .buckets().array()
                .forEach(bucket -> popularKeywords.add(bucket.key().stringValue()));  // ✅ _toString() 사용

        return popularKeywords;
    }

    public List<String> getPopularKeywordsFastest() throws IOException {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index("search_keywords")  // 검색어 인덱스 지정
                .aggregations("popular_searches", a -> a
                        .terms(t -> t
                                .field("searchText.keyword")  // ✅ keyword 필드 사용
                                .size(1000)))
                .requestCache(true) // ✅ 쿼리 캐싱 활성화 (반복 실행 시 속도 증가)
                .build();

        // Elasticsearch 검색 실행
        SearchResponse<Void> response = client.search(searchRequest, Void.class);

        // Aggregation 결과에서 추천 검색어를 추출
        List<String> popularKeywords = new ArrayList<>();
        response.aggregations()
                .get("popular_searches")
                .sterms()
                .buckets().array()
                .forEach(bucket -> popularKeywords.add(bucket.key().stringValue()));  // ✅ _toString() 사용

        return popularKeywords;
    }


    // ---------------------------------------------------------------------
    // 3. 추천 검색어 기능 (Completion Suggester 이용)
    // ---------------------------------------------------------------------
    /**
     * 입력된 쿼리를 기반으로 자동 완성 추천 검색어를 반환합니다.
     */
//    public List<String> getSuggestions(String query, String categoryValue) {
//        List<String> suggestions = new ArrayList<>();
//
//        try {
//            // Suggester 설정 (context 추가)
//            Suggester suggester = Suggester.of(s -> s
//                    .suggesters("suggestions", FieldSuggester.of(fs -> fs
//                            .completion(CompletionSuggester.of(cs -> cs
//                                    .field("suggest")
//                                    .size(5)
//                                    .skipDuplicates(true)
//                                    // 문자열 대신 CompletionContext 객체를 전달 (value 대신 context 사용)
//                                    .contexts("category", List.of(CompletionContext.of(c -> c.context(categoryValue))))
//                            ))
//                    ))
//                    .text(query)
//            );
//
//            // SearchRequest 생성
//            SearchRequest searchRequest = SearchRequest.of(sr -> sr
//                    .index("search_keywords")
//                    .suggest(suggester)
//            );
//
//            // Elasticsearch 검색 실행
//            SearchResponse<Void> response = client.search(searchRequest, Void.class);
//
//            // 결과에서 추천어 추출
//            if (response.suggest() != null && response.suggest().containsKey("suggestions")) {
//                response.suggest().get("suggestions").forEach(suggestion -> {
//                    suggestion.completion().options().forEach(option -> {
//                        suggestions.add(option.text());
//                    });
//                });
//            }
//        } catch (ElasticsearchException | IOException e) {
//            e.printStackTrace();
//        }
//
//        return suggestions;
//    }





    // ---------------------------------------------------------------------
    // 4. 도서 검색 기능 (고급 검색 쿼리, 하이라이팅 적용)
    // ---------------------------------------------------------------------
    /**
     * 책 제목을 Elasticsearch를 통해 검색합니다.
     * 여러 필드(다중 매칭, 언어별 필드 등)에 대해 boost를 적용하고,
     * DisMax 쿼리로 최적의 검색 결과를 반환하며, 하이라이팅도 적용합니다.
     *
     * @param keyword 사용자가 입력한 검색어
     * @return BookSearchResponse 객체 리스트
     */

    public List<SearchKeyword> searchKeywords(String keyword) {
        // 인덱스명과 검색할 필드 지정 (SearchKeyword 기반)
        final String INDEX = "search_keywords";
        final String FIELD_NAME = "searchText";
        final Integer SIZE = 10;

        // boost 값 설정 (필요시 조정)
        final Float KEYWORD_BOOST_VALUE = 2f;
        final Float PHRASE_BOOST_VALUE = 1.5f;
        final Float LANGUAGE_BOOST_VALUE = 1.2f;
        final Float DEFAULT_BOOST_VALUE = 1f;
        final Float PARTIAL_BOOST_VALUE = 0.5f;

        // 검색어에 한글이 포함되었는지에 따라 사용할 필드 접미사 결정 (필요에 따라 적용)
        String[] fieldSuffixes = containsKorean(keyword)
                ? new String[]{"", "_chosung", "_jamo"}
                : new String[]{"", "_engtokor"};

        // 각 필드에 대한 boost 값을 맵으로 정의
        Map<String, Float> boostValueByMultiFieldMap = Map.of(
                "", KEYWORD_BOOST_VALUE,
                ".edge", DEFAULT_BOOST_VALUE,
                ".partial", PARTIAL_BOOST_VALUE
        );

        // 다중 필드에 대해 MatchQuery 리스트 생성
        List<Query> queryList = createMatchQueryList(FIELD_NAME, fieldSuffixes, boostValueByMultiFieldMap, keyword)
                .stream()
                .map(MatchQuery::_toQuery)
                .collect(Collectors.toList());

        // 언어별 검색 필드 선택 (필요에 따라)
        String languageField = FIELD_NAME + (containsKorean(keyword) ? ".kor" : ".en");
        queryList.add(createMatchQuery(keyword, languageField, LANGUAGE_BOOST_VALUE)._toQuery());
        queryList.add(createMatchPhraseQuery(keyword, languageField, PHRASE_BOOST_VALUE)._toQuery());

        // DisMax 쿼리 구성 (여러 쿼리 중 가장 높은 점수 기준)
        DisMaxQuery disMaxQuery = new DisMaxQuery.Builder()
                .queries(queryList)
                .build();

        // 하이라이팅 설정 (검색어를 <strong> 태그로 강조)
        Highlight highlight = createHighlightFieldMap(Arrays.asList(
                FIELD_NAME,
                FIELD_NAME + ".en",
                FIELD_NAME + ".kor",
                FIELD_NAME + ".edge",
                FIELD_NAME + ".partial"
        ));

        // 검색 요청 생성
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(INDEX)
                .size(SIZE)
                .query(q -> q.disMax(disMaxQuery))
                .highlight(highlight)
                .build();

        // Elasticsearch 검색 실행
        SearchResponse<SearchKeyword> response;
        try {
            response = client.search(searchRequest, SearchKeyword.class);
        } catch (ElasticsearchException e) {
            throw new RuntimeException("Elasticsearch 검색 실패", e);
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch I/O 실패", e);
        }

        // 검색 결과 총 건수 로그 출력
        TotalHits total = response.hits().total();
        boolean isExactResult = total != null && total.relation() == TotalHitsRelation.Eq;
        log.info("There are {}{} results",
                isExactResult ? "" : "more than ",
                total != null ? total.value() : 0);

        // 검색 결과 매핑: 각 Hit에서 source() 추출
        List<Hit<SearchKeyword>> hits = response.hits().hits();
        List<SearchKeyword> result = hits.stream()
                .map(Hit::source)
                .collect(Collectors.toList());

        log.info("Search result: {}", result);
        return result;
    }

    // ---------------------------------------------------------------------
    // 4-1. 검색 헬퍼 메서드들
    // ---------------------------------------------------------------------
    private boolean containsKorean(String text) {
        return text != null && text.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
    }

    private List<MatchQuery> createMatchQueryList(String fieldName, String[] fieldSuffixes,
                                                  Map<String, Float> boostValueByMultiFieldMap, String keyword) {
        return Arrays.stream(fieldSuffixes)
                .flatMap(fieldSuffix -> boostValueByMultiFieldMap.entrySet().stream()
                        .map(entry -> createMatchQuery(keyword, fieldName + fieldSuffix + entry.getKey(), entry.getValue())))
                .collect(Collectors.toList());
    }

    private MatchQuery createMatchQuery(String keyword, String fieldName, Float boostValue) {
        return new MatchQuery.Builder()
                .query(keyword)
                .field(fieldName)
                .boost(boostValue)
                .build();
    }

    private MatchPhraseQuery createMatchPhraseQuery(String keyword, String fieldName, Float boostValue) {
        return new MatchPhraseQuery.Builder()
                .query(keyword)
                .field(fieldName)
                .boost(boostValue)
                .build();
    }

    private Highlight createHighlightFieldMap(List<String> fieldNames) {
        Map<String, HighlightField> highlightFieldMap = new HashMap<>();
        for (String fieldName : fieldNames) {
            highlightFieldMap.put(fieldName,
                    new HighlightField.Builder()
                            .preTags("<strong>")
                            .postTags("</strong>")
                            .build());
        }
        return new Highlight.Builder().fields(highlightFieldMap).build();
    }

    public void deleteAllDocuments() throws IOException {
        DeleteByQueryRequest request = DeleteByQueryRequest.of(d -> d
                .index(INDEX_NAME)
                .query(q -> q.matchAll(m -> m))
        );

        DeleteByQueryResponse response = client.deleteByQuery(request);
        log.info("전체 문서 삭제 완료: {}건 삭제됨", response.deleted());
    }
}