package com.example.SocialMedia.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenBlackListService {
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtService jwtService;
    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    public void blackList(String token) {
        Date expiration = jwtService.extractExpiration(token);
        long ttl = expiration.getTime() - System.currentTimeMillis();
        if (ttl > 0) {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(
                    key,
                    "blacklist",
                    Duration.ofMillis(ttl)
            );
        }
    }
}
