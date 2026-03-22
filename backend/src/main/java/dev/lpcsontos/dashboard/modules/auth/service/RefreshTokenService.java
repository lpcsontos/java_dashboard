package dev.lpcsontos.dashboard.modules.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Service
public class RefreshTokenService {

    private final StringRedisTemplate redis;

    public RefreshTokenService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void storeRefreshToken(Long userId, String token) {
        String key = "refresh:" + userId;
        redis.opsForValue().set(key, token, Duration.ofDays(7));
    }

    public boolean validateRefreshToken(Long userId, String token) {
        String key = "refresh:" + userId;
        String stored = redis.opsForValue().get(key);
        return stored != null && stored.equals(token);
    }

    public void deleteRefreshToken(Long userId) {
        redis.delete("refresh:" + userId);
    }

    public Optional<Long> findUserIdByToken(String token) {
        Set<String> keys = redis.keys("refresh:*");
        if (keys == null) return Optional.empty();

        for (String key : keys) {
            String stored = redis.opsForValue().get(key);
            if (stored != null && stored.equals(token)) {
                Long userId = Long.valueOf(key.split(":")[1]);
                return Optional.of(userId);
            }
        }
        return Optional.empty();
    }

}
