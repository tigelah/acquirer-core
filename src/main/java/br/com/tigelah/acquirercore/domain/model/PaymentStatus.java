package br.com.tigelah.acquirercore.domain.model;

public enum PaymentStatus {
    CREATED,
    AUTH_REQUESTED,
    AUTHORIZED_HOLD,
    AUTHORIZED,
    RISK_REJECTED,
    DECLINED,
    CAPTURE_REQUESTED,
    CAPTURED,
    SETTLED,
    PARTIALLY_REFUNDED,
    REFUNDED,
    VOIDED,
    EXPIRED
}