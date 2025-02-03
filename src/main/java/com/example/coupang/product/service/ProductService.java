package com.example.coupang.product.service;

import com.example.coupang.common.RestPage;
import com.example.coupang.product.dto.response.ProductResponseDto;

public interface ProductService {

    ProductResponseDto getProductById(Long id);

    RestPage<ProductResponseDto> getProductsV1(Integer page, Integer size, String title);

    RestPage<ProductResponseDto> getProductsV2(Integer page, Integer size, String title);

}
