package br.com.tigelah.acquirercore.application;

import br.com.tigelah.acquirercore.application.usecase.ExpireAuthorizationsUseCase;
import br.com.tigelah.acquirercore.domain.model.Payment;
import br.com.tigelah.acquirercore.domain.model.PaymentStatus;
import br.com.tigelah.acquirercore.domain.ports.EventPublisher;
import br.com.tigelah.acquirercore.domain.ports.PaymentRepository;
import br.com.tigelah.acquirercore.infrastructure.repositories.PaymentRehydrator;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpireAuthorizationsUseCaseTest {

    @Test
    void expires_authorized_before_cutoff_and_publishes_events() {
        var payments = mock(PaymentRepository.class);
        var events = mock(EventPublisher.class);

        var clock = Clock.fixed(Instant.parse("2030-01-08T00:00:00Z"), ZoneOffset.UTC);

        var p1 = new Payment(UUID.randomUUID(), "m1", "o1", 1000L, "BRL", "1111", Instant.parse("2030-01-01T00:00:00Z"));
        p1 = PaymentRehydrator.rehydrate(p1, PaymentStatus.AUTHORIZED, "SIM1");

        when(payments.findAuthorizedBefore(any(), eq(200))).thenReturn(List.of(p1));

        var uc = new ExpireAuthorizationsUseCase(payments, events, clock);

        int n = uc.execute(Duration.ofDays(7), 200);

        assertEquals(1, n);
        verify(payments, times(1)).save(any(Payment.class));
        verify(events, times(1)).publishAuthorizationExpired(any(Payment.class), eq("preauth-expirer"), eq("preauth_expired"));
    }

    @Test
    void rejects_invalid_ttl() {
        var payments = mock(PaymentRepository.class);
        var events = mock(EventPublisher.class);
        var uc = new ExpireAuthorizationsUseCase(payments, events, Clock.systemUTC());

        assertThrows(IllegalArgumentException.class, () -> uc.execute(Duration.ZERO, 10));
        assertThrows(IllegalArgumentException.class, () -> uc.execute(Duration.ofDays(-1), 10));
    }
}