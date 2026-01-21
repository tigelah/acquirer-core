package br.com.tigelah.acquirercore.infrastructure.http;

import br.com.tigelah.acquirercore.domain.ports.CardCertifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CardCertifierClient implements CardCertifier {
    private final RestTemplate rest;
    private final String baseUrl;

    public CardCertifierClient(RestTemplate rest, @Value("${integrations.card-certifier.base-url}") String baseUrl) {
        this.rest = rest;
        this.baseUrl = baseUrl;
    }

    @Override
    public CertificationResult certify(CardData card, String correlationId) {
        var headers = new HttpHeaders();
        headers.add("X-Correlation-Id", correlationId);
        var req = new HttpEntity<>(card, headers);

        var resp = rest.postForEntity(baseUrl + "/certify", req, CertificationResult.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            return new CertificationResult(false, "certifier_unavailable", "UNKNOWN");
        }
        return resp.getBody();
    }
}
