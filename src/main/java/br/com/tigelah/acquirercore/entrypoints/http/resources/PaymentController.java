package br.com.tigelah.acquirercore.entrypoints.http.resources;

import br.com.tigelah.acquirercore.application.commands.AuthorizePaymentCommand;
import br.com.tigelah.acquirercore.application.commands.CapturePaymentCommand;
import br.com.tigelah.acquirercore.application.queries.GetPaymentQuery;
import br.com.tigelah.acquirercore.application.usecase.AuthorizePaymentUseCase;
import br.com.tigelah.acquirercore.application.usecase.CapturePaymentUseCase;
import br.com.tigelah.acquirercore.application.usecase.GetPaymentUseCase;
import br.com.tigelah.acquirercore.domain.ports.CardCertifier;
import br.com.tigelah.acquirercore.entrypoints.http.dto.AuthorizeRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final AuthorizePaymentUseCase authorize;
    private final CapturePaymentUseCase capture;
    private final GetPaymentUseCase get;

    public PaymentController(AuthorizePaymentUseCase authorize, CapturePaymentUseCase capture, GetPaymentUseCase get) {
        this.authorize = authorize;
        this.capture = capture;
        this.get = get;
    }

    @PostMapping("/authorize")
    public ResponseEntity<?> authorize(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody AuthorizeRequest req
    ) {
        var cid = correlationId != null ? correlationId : UUID.randomUUID().toString();
        var out = authorize.execute(new AuthorizePaymentCommand(
                req.merchantId(), req.orderId(), req.amountCents(), req.currency(),
                new CardCertifier.CardData(req.card().pan(), req.card().holder(), req.card().expMonth(), req.card().expYear(), req.card().cvv()),
                cid, idempotencyKey
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

    @GetMapping("/{paymentId}")
    public ResponseEntity<?> get(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(get.execute(new GetPaymentQuery(paymentId)));
    }
}

