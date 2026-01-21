package br.com.tigelah.acquirercore.application.commands;

import java.util.UUID;

public record CapturePaymentCommand(UUID paymentId, String correlationId) {}
