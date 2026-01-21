package br.com.tigelah.acquirercore.domain.ports;

public interface CardCertifier {
    CertificationResult certify(CardData card, String correlationId);
    record CardData(String pan, String holder, String expMonth, String expYear, String cvv) {}
    record CertificationResult(boolean valid, String reason, String brand) {}
}

