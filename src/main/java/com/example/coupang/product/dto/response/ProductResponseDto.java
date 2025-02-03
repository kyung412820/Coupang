package com.example.coupang.product.dto.response;

import com.example.coupang.product.entity.Product;
import com.querydsl.core.annotations.QueryProjection;
import java.io.Serializable;
import lombok.Getter;

@Getter
public class ProductResponseDto implements Serializable {

    private final Long id;
    private final String productName;
    private final String contents;
    private final Long price;

    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.productName = product.getProductName();
        this.contents = product.getContents();
        this.price = product.getPrice();
    }

    @QueryProjection
    public ProductResponseDto(Long id, String productName, String contents, Long price) {
        this.id = id;
        this.productName = productName;
        this.contents = contents;
        this.price = price;
    }
}
