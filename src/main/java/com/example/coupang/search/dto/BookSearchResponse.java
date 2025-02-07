package com.example.coupang.search.dto;

import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.coupang.search.entity.BookDocument;
import lombok.Getter;
import lombok.AllArgsConstructor;


@Getter
@AllArgsConstructor
public class BookSearchResponse {
    private String title;
    private String author;
    private String publisher;

    public static BookSearchResponse from(Hit<BookDocument> hit) {
        BookDocument document = hit.source();
        return new BookSearchResponse(
                document.getTitle(),
                document.getAuthor(),
                document.getPublisher()
        );
    }
}
