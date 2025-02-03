package com.example.coupang.product.service;

import com.example.coupang.common.RestPage;
import com.example.coupang.product.dto.response.ProductResponseDto;
import com.example.coupang.product.entity.Product;
import com.example.coupang.product.repository.ProductQueryRepository;
import com.example.coupang.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductQueryRepository productQueryRepository;

    @Override
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.getById(id);
        return new ProductResponseDto(product);
    }

    @Override
    public RestPage<ProductResponseDto> getProductsV1(Integer page, Integer size, String title) {
        Pageable pageable = PageRequest.of(page - 1, size);


        return productQueryRepository.getProducts(pageable, title);
    }


    @Cacheable(value = "getProducts", key= "'products:page:' + #page + ':size' + #size", cacheManager = "ProductCacheManager")
    @Override
    public RestPage<ProductResponseDto> getProductsV2(Integer page, Integer size, String title) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return productQueryRepository.getProducts(pageable, title);
    }

}
