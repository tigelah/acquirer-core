package br.com.tigelah.acquirercore.infrastructure.repositories;


import br.com.tigelah.acquirercore.domain.model.Payment;
import br.com.tigelah.acquirercore.domain.model.PaymentStatus;

final class PaymentRehydrator {
    private PaymentRehydrator() {}

    static Payment rehydrate(Payment base, PaymentStatus status, String authCode) {
        try {
            var statusField = Payment.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(base, status);

            var authField = Payment.class.getDeclaredField("authCode");
            authField.setAccessible(true);
            authField.set(base, authCode);
            return base;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to rehydrate payment", e);
        }
    }
}
