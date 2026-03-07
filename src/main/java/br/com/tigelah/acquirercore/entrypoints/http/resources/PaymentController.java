package br.com.tigelah.acquirercore.entrypoints.http.resources;

import br.com.tigelah.acquirercore.application.commands.AuthorizePaymentCommand;
import br.com.tigelah.acquirercore.application.commands.CapturePaymentCommand;
import br.com.tigelah.acquirercore.application.commands.RequestRefundCommand;
import br.com.tigelah.acquirercore.application.commands.VoidAuthorizationCommand;
import br.com.tigelah.acquirercore.application.queries.GetPaymentQuery;
import br.com.tigelah.acquirercore.application.usecase.AuthorizePaymentUseCase;
import br.com.tigelah.acquirercore.application.usecase.CapturePaymentUseCase;
import br.com.tigelah.acquirercore.application.usecase.GetPaymentUseCase;
import br.com.tigelah.acquirercore.application.usecase.RequestRefundUseCase;
import br.com.tigelah.acquirercore.application.usecase.VoidAuthorizationUseCase;
import br.com.tigelah.acquirercore.domain.ports.CardCertifier;
import br.com.tigelah.acquirercore.entrypoints.http.dto.AuthorizeRequest;
import br.com.tigelah.acquirercore.entrypoints.http.dto.RefundRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final AuthorizePaymentUseCase authorize;
    private final CapturePaymentUseCase capture;
    private final GetPaymentUseCase get;
    private final VoidAuthorizationUseCase voidAuthorization;
    private final RequestRefundUseCase requestRefund;

    public PaymentController(
            AuthorizePaymentUseCase authorize,
            CapturePaymentUseCase capture,
            GetPaymentUseCase get,
            VoidAuthorizationUseCase voidAuthorization,
            RequestRefundUseCase requestRefund
    ) {
        this.authorize = authorize;
        this.capture = capture;
        this.get = get;
        this.voidAuthorization = voidAuthorization;
        this.requestRefund = requestRefund;
    }

    @PostMapping("/authorize")
    public ResponseEntity<?> authorize(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody AuthorizeRequest req
    ) {
        var cid = correlationId != null ? correlationId : UUID.randomUUID().toString();

        var out = authorize.execute(new AuthorizePaymentCommand(
                req.merchantId(),
                req.orderId(),
                req.amountCents(),
                req.currency(),
                new CardCertifier.CardData(
                        req.card().pan(),
                        req.card().holder(),
                        req.card().expMonth(),
                        req.card().expYear(),
                        req.card().cvv()
                ),
                cid,
                idempotencyKey,
                req.accountId(),
                req.userId(),
                req.installments()
        ));

        return ResponseEntity.ok(out);
    }

    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<?> capture(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable UUID paymentId
    ) {
        var cid = correlationId != null ? correlationId : UUID.randomUUID().toString();
        var out = capture.execute(new CapturePaymentCommand(paymentId, cid));
        return ResponseEntity.ok(out);
    }

    @PostMapping("/{paymentId}/refunds")
    public ResponseEntity<?> requestRefund(
            @PathVariable UUID paymentId,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @Valid @RequestBody RefundRequest req
    ) {
        var cid = correlationId != null ? correlationId : UUID.randomUUID().toString();

        var out = requestRefund.execute(new RequestRefundCommand(
                paymentId,
                req.amountCents(),
                req.reason(),
                cid
        ));

        return ResponseEntity.accepted().body(out);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<?> get(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(get.execute(new GetPaymentQuery(paymentId)));
    }

    @PostMapping("/{paymentId}/void")
    public ResponseEntity<?> voidAuth(
            @PathVariable UUID paymentId,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestBody(required = false) Map<String, String> body
    ) {
        var reason = body == null ? "merchant_void" : String.valueOf(body.getOrDefault("reason", "merchant_void"));
        return ResponseEntity.ok(voidAuthorization.execute(new VoidAuthorizationCommand(paymentId, correlationId, reason)));
    }
}

