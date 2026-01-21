package br.com.tigelah.acquirercore.domain.ports;

public interface BrandNetwork {
    BrandCheckResult check(String pan, String brand, String correlationId);

    record BrandCheckResult(boolean allowed, String reason) {}
}

