package com.example.coupang.product.controller;

import com.example.coupang.common.RestPage;
import com.example.coupang.product.dto.response.ProductResponseDto;
import com.example.coupang.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDto> getProduct(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @GetMapping("/v1/list")
    public ResponseEntity<RestPage<ProductResponseDto>> getProductsV1(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "10") Integer size,
        @RequestParam(required = false) String title) {
        return ResponseEntity.ok(productService.getProductsV1(page, size, title));
    }

    @GetMapping("/v2/list")
    public ResponseEntity<RestPage<ProductResponseDto>> getProductsV2(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "10") Integer size,
        @RequestParam(required = false) String title) {
        return ResponseEntity.ok(productService.getProductsV2(page, size, title));
    }
}
