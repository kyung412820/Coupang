package com.example.coupang.product.repository;

import static com.example.coupang.product.entity.QProduct.product;
import static com.example.coupang.user.entity.QUser.user;
import static org.springframework.util.StringUtils.hasText;

import com.example.coupang.product.dto.response.ProductResponseDto;
import com.example.coupang.product.dto.response.QProductResponseDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public Page<ProductResponseDto> getProducts(Pageable pageable, String title) {

        BooleanBuilder builder = new BooleanBuilder();
        if(hasText(title)) {
            builder.and(product.productName.contains(title));
        }

        List<ProductResponseDto> content = queryFactory
            .select(new QProductResponseDto(
                product.id,
                product.productName,
                product.contents,
                product.price))
            .from(product)
            .leftJoin(product.user, user)
            .where(builder)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPQLQuery<Long> countQuery = queryFactory
            .select(product.count())
            .from(product)
            .where(builder);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
