package br.com.tigelah.acquirercore.entrypoints.http.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record AuthorizeRequest(
        @NotBlank String merchantId,
        @NotBlank String orderId,
        @Positive Long amountCents,
        @NotBlank String currency,
        @NotNull @Valid CardData card,
        @NotNull UUID accountId,
        @NotNull String userId,
        @NotNull Integer installments
) {
    public record CardData(
            @NotBlank String pan,
            @NotBlank String holder,
            @NotBlank String expMonth,
            @NotBlank String expYear,
            @NotBlank String cvv
    ) {}
}