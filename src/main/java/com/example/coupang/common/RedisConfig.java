package com.example.coupang.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public RedisTemplate<String, Long> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Long> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 키 직렬화 방식을 StringRedisSerializer로 설정
        template.setKeySerializer(new StringRedisSerializer());

        // 값 직렬화 (한글 깨짐 방지)
        template.setValueSerializer(new StringRedisSerializer());

        // 값 직렬화 방식을 Jackson2JsonRedisSerializer로 설정
        Jackson2JsonRedisSerializer<Long> serializer = new Jackson2JsonRedisSerializer<>(Long.class);
        template.setValueSerializer(serializer);

        return template;
    }
}