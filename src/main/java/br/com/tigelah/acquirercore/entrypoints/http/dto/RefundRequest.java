package br.com.tigelah.acquirercore.entrypoints.http.dto;

import br.com.tigelah.acquirercore.domain.model.RefundReason;
import jakarta.validation.constraints.NotNull;

public record RefundRequest(
        Long amountCents,
        @NotNull RefundReason reason
) {
}
