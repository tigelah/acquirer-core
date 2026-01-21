package br.com.tigelah.acquirercore.domain.model;

public enum PaymentStatus {
    CREATED,
    AUTH_REQUESTED,
    RISK_REJECTED,
    AUTHORIZED,
    DECLINED,
    CAPTURE_REQUESTED,
    CAPTURED,
    SETTLED
}
