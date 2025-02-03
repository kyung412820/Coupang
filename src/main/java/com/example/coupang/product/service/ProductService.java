package com.example.coupang.product.service;

import com.example.coupang.product.dto.response.ProductResponseDto;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ProductService {

    ProductResponseDto getProductById(Long id);

    Page<ProductResponseDto> getProductsV1(Integer page, Integer size, String title);

    List<ProductResponseDto> getProductsV2(Integer page, Integer size, String title);

}
