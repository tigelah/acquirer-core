package br.com.tigelah.acquirercore.application.usecase;

import br.com.tigelah.acquirercore.application.commands.RequestRefundCommand;
import br.com.tigelah.acquirercore.application.dto.RefundOutput;
import br.com.tigelah.acquirercore.domain.model.Payment;
import br.com.tigelah.acquirercore.domain.model.PaymentFees;
import br.com.tigelah.acquirercore.domain.model.Refund;
import br.com.tigelah.acquirercore.domain.model.RefundReason;
import br.com.tigelah.acquirercore.domain.model.RefundStatus;
import br.com.tigelah.acquirercore.domain.model.RefundType;
import br.com.tigelah.acquirercore.domain.ports.PaymentRepository;
import br.com.tigelah.acquirercore.domain.ports.RefundEventPublisher;
import br.com.tigelah.acquirercore.domain.ports.RefundRepository;
import br.com.tigelah.acquirercore.domain.service.RefundFeeRecalculationService;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class RequestRefundUseCase {

    private final PaymentRepository payments;
    private final RefundRepository refunds;
    private final RefundEventPublisher refundEvents;
    private final RefundFeeRecalculationService feeRecalculationService;
    private final Clock clock;

    public RequestRefundUseCase(
            PaymentRepository payments,
            RefundRepository refunds,
            RefundEventPublisher refundEvents,
            RefundFeeRecalculationService feeRecalculationService,
            Clock clock
    ) {
        this.payments = payments;
        this.refunds = refunds;
        this.refundEvents = refundEvents;
        this.feeRecalculationService = feeRecalculationService;
        this.clock = clock;
    }

    public RefundOutput execute(RequestRefundCommand cmd) {
        if (cmd.paymentId() == null) {
            throw new IllegalArgumentException("payment_id_required");
        }

        RefundReason reason = cmd.reason();
        if (reason == null) {
            throw new IllegalArgumentException("refund_reason_required");
        }

        Payment payment = payments.getOrThrow(cmd.paymentId());

        if (!payment.canRefund()) {
            throw new IllegalStateException("refund_not_allowed");
        }

        long alreadyRefunded = refunds.sumByPaymentIdAndStatuses(
                payment.getId(),
                List.of(
                        RefundStatus.REQUESTED,
                        RefundStatus.ISSUED,
                        RefundStatus.LEDGER_APPLIED,
                        RefundStatus.SETTLEMENT_ADJUSTMENT_PENDING,
                        RefundStatus.COMPLETED
                )
        );

        if (payment.getRefundedAmountCents() != alreadyRefunded) {
            payment.setRefundedAmountCents(alreadyRefunded);
            payments.save(payment);
        }

        long availableAmount = payment.availableToRefund();
        long requestedAmount = resolveRequestedAmount(cmd.amountCents(), availableAmount);

        RefundType refundType = payment.refundTypeFor(requestedAmount);

        PaymentFees reversedFees = feeRecalculationService.calculateProportionalReversal(
                payment.getAmountCents(),
                requestedAmount,
                payment.getFees()
        );

        Refund refund = new Refund(
                UUID.randomUUID(),
                payment.getId(),
                payment.getMerchantId(),
                requestedAmount,
                payment.getCurrency(),
                refundType,
                reason,
                RefundStatus.REQUESTED,
                reversedFees,
                Instant.now(clock)
        );

        refunds.save(refund);
        refundEvents.publishRefundRequested(refund, payment, normalizeCorrelationId(cmd.correlationId()));

        return RefundOutput.from(refund);
    }

    private long resolveRequestedAmount(Long amountCents, long availableAmount) {
        if (availableAmount <= 0) {
            throw new IllegalStateException("no_refundable_balance");
        }
        if (amountCents == null) {
            return availableAmount;
        }
        if (amountCents <= 0) {
            throw new IllegalArgumentException("invalid_refund_amount");
        }
        if (amountCents > availableAmount) {
            throw new IllegalArgumentException("refund_amount_exceeds_available");
        }
        return amountCents;
    }

    private String normalizeCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return correlationId;
    }
}