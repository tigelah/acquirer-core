package br.com.tigelah.acquirercore.application.usecase;

import br.com.tigelah.acquirercore.domain.ports.EventPublisher;
import br.com.tigelah.acquirercore.domain.ports.PaymentRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class ExpireAuthorizationsUseCase {

    private final PaymentRepository payments;
    private final EventPublisher events;
    private final Clock clock;

    public ExpireAuthorizationsUseCase(PaymentRepository payments, EventPublisher events, Clock clock) {
        this.payments = payments;
        this.events = events;
        this.clock = clock;
    }

    public int execute(Duration ttl, int batchLimit) {
        if (ttl == null || ttl.isNegative() || ttl.isZero()) throw new IllegalArgumentException("ttl_invalid");
        if (batchLimit <= 0) batchLimit = 200;

        Instant now = Instant.now(clock);
        Instant cutoff = now.minus(ttl);

        var candidates = payments.findAuthorizedBefore(cutoff, batchLimit);

        var expired = 0;
        for (var p : candidates) {
            p.expireAuthorization();
            payments.save(p);

            events.publishAuthorizationExpired(p, "preauth-expirer", "preauth_expired");
            expired++;
        }

        return expired;
    }
}
