package br.com.tigelah.acquirercore.infrastructure.scheduling;

import br.com.tigelah.acquirercore.application.usecase.ExpireAuthorizationsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class PreAuthExpirationScheduler {
    private static final Logger log = LoggerFactory.getLogger(PreAuthExpirationScheduler.class);

    private final ExpireAuthorizationsUseCase useCase;
    private final Duration ttl;
    private final int limit;

    public PreAuthExpirationScheduler(
            ExpireAuthorizationsUseCase useCase,
            PreAuthProperties props
    ) {
        this.useCase = useCase;
        this.ttl = Duration.ofDays(props.ttlDays());
        this.limit = props.batchLimit();
    }

    @Scheduled(fixedDelayString = "${preauth.expirer.delay-ms:60000}")
    public void tick() {
        try {
            int n = useCase.execute(ttl, limit);
            if (n > 0) log.info("preauth_expired count={}", n);
        } catch (Exception e) {
            log.error("preauth_expirer_failed", e);
        }
    }
}
