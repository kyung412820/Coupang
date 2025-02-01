package com.example.coupang.product.service;

import com.example.coupang.product.dto.response.ProductResponseDto;
import com.example.coupang.product.entity.Product;
import com.example.coupang.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.getById(id);
        return new ProductResponseDto(product);
    }
}
