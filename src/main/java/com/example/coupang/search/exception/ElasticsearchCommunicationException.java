package com.example.coupang.search.exception;

public class ElasticsearchCommunicationException extends RuntimeException {
    public ElasticsearchCommunicationException(String message) {
        super(message);
    }

    public ElasticsearchCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
