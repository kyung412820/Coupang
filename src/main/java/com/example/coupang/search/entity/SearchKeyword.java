package com.example.coupang.search.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import java.time.LocalDateTime;

@Data
@Document(indexName = "search_keywords")
public class SearchKeyword {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String searchIc;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String searchText;

    @Field(type = FieldType.Integer)
    private int count;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;

    @CompletionField
    private Completion suggest;
}