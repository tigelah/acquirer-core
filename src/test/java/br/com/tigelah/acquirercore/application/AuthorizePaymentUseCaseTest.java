package br.com.tigelah.acquirercore.application;

import br.com.tigelah.acquirercore.application.commands.AuthorizePaymentCommand;
import br.com.tigelah.acquirercore.application.security.PanHasher;
import br.com.tigelah.acquirercore.application.usecase.AuthorizePaymentUseCase;
import br.com.tigelah.acquirercore.domain.model.Payment;
import br.com.tigelah.acquirercore.domain.ports.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizePaymentUseCaseTest {

    @Test
    void should_be_idempotent_by_key() {
        var payments = mock(PaymentRepository.class);
        var idem = mock(IdempotencyStore.class);
        var events = mock(EventPublisher.class);
        var certifier = mock(CardCertifier.class);
        var brand = mock(BrandNetwork.class);
        var clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

        var paymentId = UUID.randomUUID();
        when(idem.get("k1")).thenReturn(Optional.of(paymentId));
        when(payments.getOrThrow(paymentId)).thenReturn(
                new Payment(paymentId, "m1","o1",1000L,"BRL","1111", Instant.now(clock))
        );

        var uc = new AuthorizePaymentUseCase(payments, idem, events, certifier, brand, clock);

        var out = uc.execute(new AuthorizePaymentCommand(
                "m1","o1",1000L,"BRL",
                new CardCertifier.CardData("4111111111111111","JOAO","12","2030","123"),
                "c1","k1",
                UUID.randomUUID(),
                "user-1",
                6
        ));

        assertEquals(paymentId, out.id());
        verify(events, never()).publishAuthorizeRequested(any(), any(), any());
    }

    @Test
    void should_certify_and_enqueue_event_with_limits_context() {
        var payments = mock(PaymentRepository.class);
        var idem = mock(IdempotencyStore.class);
        var events = mock(EventPublisher.class);
        var certifier = mock(CardCertifier.class);
        var brand = mock(BrandNetwork.class);
        var clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

        when(idem.get("k1")).thenReturn(Optional.empty());
        when(payments.findByMerchantAndOrder("m1","o1")).thenReturn(Optional.empty());
        when(certifier.certify(any(), eq("c1"))).thenReturn(new CardCertifier.CertificationResult(true, "ok", "VISA"));
        when(brand.check(anyString(), eq("VISA"), eq("c1"))).thenReturn(new BrandNetwork.BrandCheckResult(true, "ok"));
        when(idem.putIfAbsent(eq("k1"), any())).thenReturn(true);

        var uc = new AuthorizePaymentUseCase(payments, idem, events, certifier, brand, clock);

        var accountId = UUID.randomUUID();
        var pan = "4111111111111111";

        uc.execute(new AuthorizePaymentCommand(
                "m1","o1",1000L,"BRL",
                new CardCertifier.CardData(pan,"JOAO","12","2030","123"),
                "c1","k1",
                accountId,
                "user-1",
                6
        ));

        var captor = ArgumentCaptor.forClass(Payment.class);
        verify(payments).save(captor.capture());

        var saved = captor.getValue();
        assertEquals("m1", saved.getMerchantId());
        assertEquals("1111", saved.getPanLast4());
        assertEquals(6, saved.getInstallments());
        assertEquals(accountId, saved.getAccountId());
        assertEquals("user-1", saved.getUserId());
        assertEquals(PanHasher.sha256(pan), saved.getPanHash());

        verify(events).publishAuthorizeRequested(any(Payment.class), eq("c1"), eq("k1"));
    }
}