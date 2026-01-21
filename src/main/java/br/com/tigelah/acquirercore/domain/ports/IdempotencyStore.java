package br.com.tigelah.acquirercore.domain.ports;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyStore {
    Optional<UUID> get(String key);
    boolean putIfAbsent(String key, UUID paymentId); // true if stored
}
