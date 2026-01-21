package br.com.tigelah.acquirercore.infrastructure.http;

import br.com.tigelah.acquirercore.domain.ports.CardCertifier;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationClientsTest {

    WireMockServer certifier;
    WireMockServer brand;

    @BeforeEach
    void setup() {
        certifier = new WireMockServer(0);
        certifier.start();
        brand = new WireMockServer(0);
        brand.start();
    }

    @AfterEach
    void teardown() {
        certifier.stop();
        brand.stop();
    }

    @Test
    void should_call_certifier_and_brand() {
        certifier.stubFor(post(urlEqualTo("/certify"))
                .willReturn(okJson("{\"valid\":true,\"reason\":\"ok\",\"brand\":\"VISA\"}")));

        brand.stubFor(post(urlEqualTo("/brand/check"))
                .willReturn(okJson("{\"allowed\":true,\"reason\":\"ok\"}")));

        var rest = new RestTemplate();
        var c = new CardCertifierClient(rest, "http://localhost:" + certifier.port());
        var b = new BrandNetworkClient(rest, "http://localhost:" + brand.port());

        var cert = c.certify(new CardCertifier.CardData("4111111111111111","JOAO","12","2030","123"), "c1");
        assertTrue(cert.valid());
        assertEquals("VISA", cert.brand());

        var chk = b.check("4111111111111111", "VISA", "c1");
        assertTrue(chk.allowed());

        certifier.verify(1, postRequestedFor(urlEqualTo("/certify")));
        brand.verify(1, postRequestedFor(urlEqualTo("/brand/check")));
    }
}
