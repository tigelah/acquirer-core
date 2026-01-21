package br.com.tigelah.acquirercore.infrastructure.config;

import br.com.tigelah.acquirercore.application.usecase.AuthorizePaymentUseCase;
import br.com.tigelah.acquirercore.application.usecase.CapturePaymentUseCase;
import br.com.tigelah.acquirercore.application.usecase.GetPaymentUseCase;
import br.com.tigelah.acquirercore.domain.ports.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class UseCaseConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public AuthorizePaymentUseCase authorizePaymentUseCase(
            PaymentRepository payments,
            IdempotencyStore idempotency,
            EventPublisher events,
            CardCertifier certifier,
            BrandNetwork brandNetwork,
            Clock clock
    ) {
        return new AuthorizePaymentUseCase(payments, idempotency, events, certifier, brandNetwork, clock);
    }

    @Bean
    public CapturePaymentUseCase capturePaymentUseCase(PaymentRepository payments, EventPublisher events) {
        return new CapturePaymentUseCase(payments, events);
    }

    @Bean
    public GetPaymentUseCase getPaymentUseCase(PaymentRepository payments) {
        return new GetPaymentUseCase(payments);
    }
}
