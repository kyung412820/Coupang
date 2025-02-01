package com.example.coupang.product.service;

import com.example.coupang.product.dto.response.ProductResponseDto;

public interface ProductService {

    ProductResponseDto getProductById(Long id);

}
