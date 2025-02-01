package com.example.coupang.product.dto.response;

import com.example.coupang.product.entity.Product;

public record ProductResponseDto(Long id, String productName, String contents, Long price) {

    public ProductResponseDto(Product product) {
        this(product.getId(), product.getProductName(), product.getContents(), product.getPrice());
    }
}
