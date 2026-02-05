package br.com.tigelah.acquirercore.infrastructure.http;

import br.com.tigelah.acquirercore.domain.ports.CardCertifier;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import com.github.tomakehurst.wiremock.WireMockServer;


import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;


class IntegrationClientsTest {

    private WireMockServer certifier;
    private WireMockServer brand;

    @BeforeEach
    void setup() {
        certifier = new WireMockServer(wireMockConfig().dynamicPort());
        certifier.start();

        brand = new WireMockServer(wireMockConfig().dynamicPort());
        brand.start();

        configureFor("localhost", certifier.port());
    }

    @AfterEach
    void teardown() {
        if (certifier != null && certifier.isRunning()) certifier.stop();
        if (brand != null && brand.isRunning()) brand.stop();
    }

    @Test
    void should_call_certifier_and_brand() {
        certifier.stubFor(post(urlEqualTo("/certify"))
                .withHeader("X-Correlation-Id", equalTo("c1"))
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(matchingJsonPath("$.pan", equalTo("4111111111111111")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
              {"valid":true,"reason":"ok","brand":"VISA"}
            """)));

        brand.stubFor(post(urlEqualTo("/brand/check"))
                .withHeader("X-Correlation-Id", equalTo("c1"))
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(matchingJsonPath("$.pan", equalTo("4111111111111111")))
                .withRequestBody(matchingJsonPath("$.brand", equalTo("VISA")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
              {"allowed":true,"reason":"ok"}
            """)));

    var rest = new RestTemplate();

    var certifierClient = new CardCertifierClient(rest, "http://localhost:" + certifier.port());
    var brandClient = new BrandNetworkClient(rest, "http://localhost:" + brand.port());

    var cert = certifierClient.certify(
        new CardCertifier.CardData("4111111111111111", "JOAO", "12", "2030", "123"),
        "c1"
    );

    assertTrue(cert.valid());
    assertEquals("VISA", cert.brand());

    var chk = brandClient.check("4111111111111111", "VISA", "c1");
    assertTrue(chk.allowed());

    certifier.verify(1, postRequestedFor(urlEqualTo("/certify"))
        .withHeader("X-Correlation-Id", equalTo("c1")));

    brand.verify(1, postRequestedFor(urlEqualTo("/brand/check"))
        .withHeader("X-Correlation-Id", equalTo("c1")));
  }

  @Test
  void should_fail_when_certifier_returns_invalid_card() {
    certifier.stubFor(post(urlEqualTo("/certify"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody("""
        {"valid":false,"reason":"luhn_failed","brand":"UNKNOWN"}
        """)));

var rest = new RestTemplate();
var certifierClient = new CardCertifierClient(rest, "http://localhost:" + certifier.port());

var cert = certifierClient.certify(
    new CardCertifier.CardData("4111111111111112", "JOAO", "12", "2030", "123"),
    "c1"
);

assertFalse(cert.valid());
assertEquals("UNKNOWN", cert.brand());
}

@Test
void should_fail_when_brand_rejects() {

certifier.stubFor(post(urlEqualTo("/certify"))
    .willReturn(okJson("""
        {"valid":true,"reason":"ok","brand":"VISA"}
        """)));

    brand.stubFor(post(urlEqualTo("/brand/check"))
        .willReturn(okJson("""
        {"allowed":false,"reason":"bin_blocked"}
        """)));

    var rest = new RestTemplate();
    var certifierClient = new CardCertifierClient(rest, "http://localhost:" + certifier.port());
    var brandClient = new BrandNetworkClient(rest, "http://localhost:" + brand.port());

    var cert = certifierClient.certify(
        new CardCertifier.CardData("4111111111111111", "JOAO", "12", "2030", "123"),
        "c1"
    );
    assertTrue(cert.valid());
    assertEquals("VISA", cert.brand());

    var chk = brandClient.check("4111111111111111", cert.brand(), "c1");
    assertFalse(chk.allowed());
    assertEquals("bin_blocked", chk.reason());
  }
}