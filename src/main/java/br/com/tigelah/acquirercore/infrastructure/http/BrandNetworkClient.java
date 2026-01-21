package br.com.tigelah.acquirercore.infrastructure.http;

import br.com.tigelah.acquirercore.domain.ports.BrandNetwork;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class BrandNetworkClient implements BrandNetwork {
    private final RestTemplate rest;
    private final String baseUrl;

    public BrandNetworkClient(RestTemplate rest, @Value("${integrations.brand-network.base-url}") String baseUrl) {
        this.rest = rest;
        this.baseUrl = baseUrl;
    }

    @Override
    public BrandCheckResult check(String pan, String brand, String correlationId) {
        var headers = new HttpHeaders();
        headers.add("X-Correlation-Id", correlationId);

        var payload = Map.of("pan", pan, "brand", brand);
        var req = new HttpEntity<>(payload, headers);

        var resp = rest.postForEntity(baseUrl + "/brand/check", req, BrandCheckResult.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            return new BrandCheckResult(false, "brand_network_unavailable");
        }
        return resp.getBody();
    }
}
