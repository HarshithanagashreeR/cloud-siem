package com.amazon.siem.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimiter {
    private static final Logger logger = LoggerFactory.getLogger(RateLimiter.class);

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    // Local fallback in case Redis connection fails
    private final ConcurrentHashMap<String, TokenBucket> localBuckets = new ConcurrentHashMap<>();

    private static final int MAX_TOKENS = 100;
    private static final int REFILL_RATE_PER_SECOND = 10;

    public boolean isAllowed(String key) {
        try {
            if (redisTemplate != null && redisTemplate.getConnectionFactory() != null) {
                String redisKey = "rate_limit:" + key;
                Long count = redisTemplate.opsForValue().increment(redisKey, 1);
                if (count != null && count == 1) {
                    redisTemplate.expire(redisKey, 10, TimeUnit.SECONDS); // Limit window to 10 seconds
                }
                // Allow up to 100 requests in 10 seconds
                if (count != null && count > 100) {
                    logger.warn("Rate limit exceeded for key: {}", key);
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
            logger.warn("Redis rate limiter failed, falling back to in-memory rate limiter: {}", e.getMessage());
        }

        // Fallback to local token bucket
        TokenBucket bucket = localBuckets.computeIfAbsent(key, k -> new TokenBucket(MAX_TOKENS, REFILL_RATE_PER_SECOND));
        return bucket.tryConsume();
    }

    private static class TokenBucket {
        private final long capacity;
        private final double refillRate;
        private double tokens;
        private Instant lastRefill;

        public TokenBucket(long capacity, double refillRate) {
            this.capacity = capacity;
            this.refillRate = refillRate;
            this.tokens = capacity;
            this.lastRefill = Instant.now();
        }

        public synchronized boolean tryConsume() {
            refill();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        private void refill() {
            Instant now = Instant.now();
            double elapsedSeconds = DurationBetween(lastRefill, now);
            tokens = Math.min(capacity, tokens + elapsedSeconds * refillRate);
            lastRefill = now;
        }

        private double DurationBetween(Instant start, Instant end) {
            return (end.toEpochMilli() - start.toEpochMilli()) / 1000.0;
        }
    }
}
