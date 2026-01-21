package br.com.tigelah.acquirercore.infrastructure.redis;

import br.com.tigelah.acquirercore.domain.ports.IdempotencyStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
public class RedisIdempotencyStore implements IdempotencyStore {
    private static final Duration TTL = Duration.ofHours(6);

    private final StringRedisTemplate redis;

    public RedisIdempotencyStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public Optional<UUID> get(String key) {
        var v = redis.opsForValue().get(key);
        if (v == null || v.isBlank()) return Optional.empty();
        return Optional.of(UUID.fromString(v));
    }

    @Override
    public boolean putIfAbsent(String key, UUID paymentId) {
        Boolean ok = redis.opsForValue().setIfAbsent(key, paymentId.toString(), TTL);
        return ok != null && ok;
    }
}
