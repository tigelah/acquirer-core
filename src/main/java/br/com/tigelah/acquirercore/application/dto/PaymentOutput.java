package br.com.tigelah.acquirercore.application.dto;

import br.com.tigelah.acquirercore.domain.model.Payment;
import br.com.tigelah.acquirercore.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentOutput(
        UUID id,
        String merchantId,
        String orderId,
        Long amountCents,
        String currency,
        String panLast4,
        PaymentStatus status,
        String authCode,
        Instant createdAt
) {
    public static PaymentOutput from(Payment p) {
        return new PaymentOutput(
                p.getId(), p.getMerchantId(), p.getOrderId(),
                p.getAmountCents(), p.getCurrency(), p.getPanLast4(),
                p.getStatus(), p.getAuthCode(), p.getCreatedAt()
        );
    }
}
