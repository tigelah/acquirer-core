package br.com.tigelah.acquirercore.application;

import br.com.tigelah.acquirercore.application.commands.CapturePaymentCommand;
import br.com.tigelah.acquirercore.application.usecase.CapturePaymentUseCase;
import br.com.tigelah.acquirercore.domain.model.Payment;
import br.com.tigelah.acquirercore.domain.model.PaymentStatus;
import br.com.tigelah.acquirercore.domain.ports.EventPublisher;
import br.com.tigelah.acquirercore.domain.ports.PaymentRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CapturePaymentUseCaseTest {

    @Test
    void should_mark_capture_requested_and_publish_event() {
        var repo = mock(PaymentRepository.class);
        var events = mock(EventPublisher.class);

        var id = UUID.randomUUID();
        var p = new Payment(id, "m1", "o1", 1000L, "BRL", "1111", Instant.now());
        try {
            var f = Payment.class.getDeclaredField("status"); f.setAccessible(true); f.set(p, PaymentStatus.AUTHORIZED);
        } catch (Exception e) { throw new RuntimeException(e); }

        when(repo.getOrThrow(id)).thenReturn(p);

        var uc = new CapturePaymentUseCase(repo, events);
        var out = uc.execute(new CapturePaymentCommand(id, "c1"));

        assertEquals(PaymentStatus.CAPTURE_REQUESTED, out.status());
        verify(repo).save(any());
        verify(events).publishCaptureRequested(any(), eq("c1"));
    }
}