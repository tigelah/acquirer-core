# ADR-0002: Validação de cartão via “certificadora” e “bandeiras” simuladas

- **Status:** Aceito
- **Data:** 2026-01-19
- **Decisores:** Time de Plataforma / Arquitetura
- **Serviço:** `acquirer-core`
- **Dependências:** `card-certifier`, `brand-network-hub`

## Contexto e Problema

Para simular um fluxo realista de adquirência, a autorização não pode aceitar qualquer PAN aleatório.
Precisamos simular verificações típicas de rede:

- Validação básica (Luhn, validade, CVV, blacklist, etc.)
- Políticas por bandeira/BIN (bloqueios, regras de roteamento, erros temporários)

Também queremos separar responsabilidades:
- `acquirer-core` orquestra e mantém o estado do pagamento.
- Serviços externos simulados representam terceiros (certificadora e rede de bandeiras).

## Decisão

- Antes de criar/avançar um pagamento em `authorize`, o `acquirer-core` chama:
  1. `card-certifier` via HTTP (`/certify`) para validação básica.
  2. `brand-network-hub` via HTTP (`/brands/validate`) para políticas por BIN/marca.

Se qualquer validação falhar, retornar **422 Unprocessable Entity** com motivo.
Falhas temporárias (timeout/5xx) podem retornar **503** conforme política (no simulador, pode ser configurável).

## Justificativas

- Simula integrações reais sem depender de terceiros.
- Permite testar resilência (timeouts, circuit breaker, retries).
- Mantém o domínio do `acquirer-core` limpo: validação externa fica em portas (`CardCertifierClient`, `BrandNetworkClient`).

## Consequências

- Adiciona dependências HTTP ao caminho de autorização.
- Necessita de testes com stubs (WireMock) para evitar flakiness.
- Exige timeouts e política de retry bem definida.

## Alternativas Consideradas

- Embutir Luhn e políticas no próprio `acquirer-core`:
  - Rejeitado para manter separação e simular o mundo real.

## Observabilidade

- Métricas por dependência:
  - `http.client.card_certifier.latency`
  - `http.client.brand_network.latency`
  - `http.client.*.errors`
- Logs com `correlationId` incluindo resultado e motivo.
