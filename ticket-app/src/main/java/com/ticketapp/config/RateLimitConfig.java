package com.ticketapp.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

@Configuration
@Profile("!test")
public class RateLimitConfig {
    @Value("${app.redis.url:redis://localhost:6379}")
    private String redisUrl;

    // 1. Doğrudan Docker'daki Redis'e bağlanıyoruz
    @Bean
    public RedisClient redisClient() {
        return RedisClient.create(redisUrl);
    }

    // 2. ProxyManager
    @Bean
    public ProxyManager<byte[]> proxyManager(RedisClient redisClient) {
        return LettuceBasedProxyManager.builderFor(redisClient)
                .withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(1)))
                .build();
    }

    // 3. Limit Kuralları (Dakikada 5 istek)
    @Bean
    public BucketConfiguration bucketConfiguration() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(40, Refill.intervally(40, Duration.ofSeconds(10))))
                .build();
    }
}