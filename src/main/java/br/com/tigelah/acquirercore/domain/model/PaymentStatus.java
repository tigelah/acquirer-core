package br.com.tigelah.acquirercore.domain.model;

public enum PaymentStatus {
    CREATED,
    AUTH_REQUESTED,
    RISK_REJECTED,
    AUTHORIZED,
    AUTHORIZED_HOLD,
    DECLINED,
    CAPTURE_REQUESTED,
    CAPTURED,
    SETTLED,
    VOIDED,
    EXPIRED
}