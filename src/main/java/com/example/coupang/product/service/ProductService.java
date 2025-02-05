package com.example.coupang.product.service;

import com.example.coupang.common.RestPage;
import com.example.coupang.product.dto.response.ProductResponseDto;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponseDto getProductById(Long id);

    RestPage<ProductResponseDto> getProductsV1(Pageable pageable, String title);

    RestPage<ProductResponseDto> getProductsV2(Pageable pageable, String title);

}
