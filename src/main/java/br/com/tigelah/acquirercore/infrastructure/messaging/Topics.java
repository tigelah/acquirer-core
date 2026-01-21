package br.com.tigelah.acquirercore.infrastructure.messaging;

public final class Topics {
    private Topics() {}

    public static final String PAYMENT_AUTHORIZE_REQUESTED = "payment.authorize.requested";
    public static final String PAYMENT_CAPTURE_REQUESTED = "payment.capture.requested";

    public static final String PAYMENT_RISK_APPROVED = "payment.risk.approved";
    public static final String PAYMENT_RISK_REJECTED = "payment.risk.rejected";
    public static final String PAYMENT_AUTHORIZED = "payment.authorized";
    public static final String PAYMENT_DECLINED = "payment.declined";
    public static final String PAYMENT_CAPTURED = "payment.captured";

    public static final String SETTLEMENT_COMPLETED = "settlement.completed";
}
