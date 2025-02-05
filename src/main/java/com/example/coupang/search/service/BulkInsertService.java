package com.example.coupang.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.example.coupang.search.entity.SearchKeyword;
import com.example.coupang.search.repository.SearchKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkInsertService {

    private final ElasticsearchClient client;
    private final SearchKeywordGenerator generator;
    private final SearchKeywordRepository repository;

    /**
     * Elasticsearch에 다량의 랜덤 검색어 데이터를 삽입하는 메서드 (Elasticsearch 8.x 버전)
     */
    public void insertBulkData(int totalRecords) {
        try {
            int batchSize = 500; // 한 번에 저장할 개수
            List<BulkOperation> bulkOperations = new ArrayList<>();

            for (int i = 0; i < totalRecords; i++) {
                SearchKeyword keyword = generator.generateRandomKeyword();

                // Bulk 작업에 추가
                bulkOperations.add(
                        BulkOperation.of(op -> op
                                .index(idx -> idx
                                        .index("search_keywords")
                                        .id(keyword.getId())
                                        .document(keyword)
                                )
                        )
                );

                // 배치 저장 (500개 단위)
                if (bulkOperations.size() >= batchSize) {
                    executeBulkRequest(bulkOperations);
                    bulkOperations.clear();
                }
            }

            // 남은 데이터 저장
            if (!bulkOperations.isEmpty()) {
                executeBulkRequest(bulkOperations);
            }

            log.info("{}개의 데이터를 Elasticsearch에 저장 완료", totalRecords);
        } catch (Exception e) {
            log.error("Bulk Insert Error", e);
        }
    }

    /**
     * Bulk API 실행 메서드 (Elasticsearch 8.x)
     */
    private void executeBulkRequest(List<BulkOperation> bulkOperations) throws IOException {
        BulkRequest bulkRequest = new BulkRequest.Builder()
                .operations(bulkOperations)
                .build();

        BulkResponse bulkResponse = client.bulk(bulkRequest);

        if (bulkResponse.errors()) {
            log.error("Bulk Insert Error 발생: {}", bulkResponse.toString());
        }
    }
}
