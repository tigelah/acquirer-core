package br.com.tigelah.acquirercore.application;

import br.com.tigelah.acquirercore.application.commands.VoidAuthorizationCommand;
import br.com.tigelah.acquirercore.application.usecase.VoidAuthorizationUseCase;
import br.com.tigelah.acquirercore.domain.model.Payment;
import br.com.tigelah.acquirercore.domain.model.PaymentStatus;
import br.com.tigelah.acquirercore.domain.ports.EventPublisher;
import br.com.tigelah.acquirercore.domain.ports.PaymentRepository;
import br.com.tigelah.acquirercore.infrastructure.repositories.PaymentRehydrator;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VoidAuthorizationUseCaseTest {

    @Test
    void voids_when_authorized_and_publishes_event() {
        var payments = mock(PaymentRepository.class);
        var events = mock(EventPublisher.class);

        var paymentId = UUID.randomUUID();
        var p = new Payment(paymentId, "m1", "o1", 1000L, "BRL", "1111", Instant.parse("2030-01-01T00:00:00Z"));
        p = PaymentRehydrator.rehydrate(p, PaymentStatus.AUTHORIZED, "SIM123");

        when(payments.getOrThrow(paymentId)).thenReturn(p);

        var uc = new VoidAuthorizationUseCase(payments, events);

        var out = uc.execute(new VoidAuthorizationCommand(paymentId, "c1", "merchant_void"));

        assertEquals(paymentId, out.id());
        assertEquals("VOIDED", out.status());

        verify(payments, times(1)).save(any(Payment.class));
        verify(events, times(1)).publishAuthorizationVoided(any(Payment.class), eq("c1"), eq("merchant_void"));
    }

    @Test
    void throws_when_not_authorized_and_does_not_publish() {
        var payments = mock(PaymentRepository.class);
        var events = mock(EventPublisher.class);

        var paymentId = UUID.randomUUID();
        var p = new Payment(paymentId, "m1", "o1", 1000L, "BRL", "1111", Instant.parse("2030-01-01T00:00:00Z"));
        // status CREATED (não autorizado)
        when(payments.getOrThrow(paymentId)).thenReturn(p);

        var uc = new VoidAuthorizationUseCase(payments, events);

        assertThrows(IllegalStateException.class,
                () -> uc.execute(new VoidAuthorizationCommand(paymentId, "c1", "merchant_void")));

        verify(payments, never()).save(any());
        verify(events, never()).publishAuthorizationVoided(any(), anyString(), anyString());
    }
}
