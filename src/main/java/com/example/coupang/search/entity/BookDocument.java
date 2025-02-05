package com.example.coupang.search.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "books")  // ✅ Elasticsearch에서 저장될 인덱스 이름 지정
public class BookDocument {

    @Id  // ✅ Elasticsearch의 ID 필드
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;  // ✅ 책 제목

    @Field(type = FieldType.Text, analyzer = "standard")
    private String author;  // ✅ 저자

    @Field(type = FieldType.Text, analyzer = "standard")
    private String publisher;  // ✅ 출판사

    @Field(type = FieldType.Keyword)
    private String category;  // ✅ 책 카테고리 (예: 소설, 과학, 역사 등)

    @Field(type = FieldType.Integer)
    private int publicationYear;  // ✅ 출판 연도

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;  // ✅ 책 설명

    @Field(type = FieldType.Integer)
    private int viewCount;  // ✅ 조회수 (검색 결과 가중치에 활용 가능)
}
