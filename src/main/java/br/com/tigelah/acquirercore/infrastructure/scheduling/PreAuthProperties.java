package br.com.tigelah.acquirercore.infrastructure.scheduling;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "preauth")
public record PreAuthProperties(
        int ttlDays,
        int batchLimit
) {
    public PreAuthProperties() {
        this(7, 200);
    }
}
