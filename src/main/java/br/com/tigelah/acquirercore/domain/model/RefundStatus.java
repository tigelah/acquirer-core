package br.com.tigelah.acquirercore.domain.model;

public enum RefundStatus {
    REQUESTED,
    ISSUED,
    LEDGER_APPLIED,
    SETTLEMENT_ADJUSTMENT_PENDING,
    COMPLETED,
    FAILED
}
