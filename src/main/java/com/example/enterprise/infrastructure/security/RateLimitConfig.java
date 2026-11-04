package com.example.enterprise.infrastructure.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitConfig {

    private static final int CAPACITY = 5;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);
    private static final Duration TTL_CLEANUP = Duration.ofMinutes(30);

    private final Map<String, CachedBucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key) {
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(entry ->
                now - entry.getValue().lastAccess > TTL_CLEANUP.toMillis());

        CachedBucket cached = buckets.compute(key, (k, existing) -> {
            if (existing == null) {
                return new CachedBucket(createBucket(), now);
            }
            existing.lastAccess = now;
            return existing;
        });
        return cached.bucket;
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(CAPACITY, Refill.greedy(CAPACITY, REFILL_PERIOD));
        return Bucket.builder().addLimit(limit).build();
    }

    private static final class CachedBucket {
        private final Bucket bucket;
        private volatile long lastAccess;

        private CachedBucket(Bucket bucket, long lastAccess) {
            this.bucket = bucket;
            this.lastAccess = lastAccess;
        }
    }
}
