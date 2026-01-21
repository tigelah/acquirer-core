# ADR-0001: Idempotência e Outbox Pattern no acquirer-core

- **Status:** Aceito
- **Data:** 2026-01-19
- **Decisores:** Time de Plataforma / Arquitetura
- **Contexto:** Simulador E2E de adquirência (Java 21 + Spring Boot + Kafka + Postgres + Redis)
- **Serviço:** `acquirer-core`

## Contexto e Problema

O `acquirer-core` implementa operações de pagamento com efeitos colaterais relevantes (criação de pagamento, mudança de status, publicação de eventos Kafka).
Em ambientes reais (e no simulador), requisições podem ser reenviadas por timeout, retries automáticos do merchant, falhas intermitentes e duplicidade do usuário.

Há dois riscos principais:

1. **Duplicidade de transações/eventos** (p.ex. dupla autorização do mesmo pedido).
2. **Inconsistência entre banco e mensageria** (p.ex. salvou o pagamento mas não publicou o evento em Kafka, ou vice-versa).

## Decisão

1. **Idempotência para `POST /payments/authorize`:**
   - Exigir header `Idempotency-Key`.
   - Persistir o mapeamento `Idempotency-Key -> paymentId` em **Redis** com TTL.
   - Repetições com a mesma chave retornam **o mesmo `paymentId`** e **não geram efeitos colaterais adicionais**.

2. **Outbox Pattern para publicação Kafka:**
   - Ao executar um caso de uso que exige publicação de evento (ex.: `authorize`, `capture`), salvar:
     - a entidade de negócio (`Payment`) e
     - um registro `OutboxEvent`
     **na mesma transação** do Postgres.
   - Um **publisher assíncrono** (scheduler/worker) publica o evento no Kafka e marca como `SENT`.
   - Implementar **retry/backoff** e contagem de tentativas (`attempts`).

## Justificativas

### Por que Idempotência?
- Evita **cobranças duplicadas** e divergência de estados no fluxo E2E.
- Suporta retries automáticos do merchant/POS sem risco de duplicidade.
- Reduz custos operacionais (suporte, chargeback, reconciliação).

### Por que Redis (vs. tabela relacional)?
- Latência baixa para caminho crítico da autorização.
- TTL simples para expiração.
- Bom ajuste para “dedupe chave -> id”.
- Alternativa válida: tabela relacional com constraint única + retorno do registro existente (não adotada para simplificar e acelerar o caminho crítico).

### Por que Outbox?
- Kafka não participa de transações ACID com Postgres.
- Sem Outbox, existe janela para “commit no DB e falha ao publicar” (ou publicação duplicada por retry).
- Outbox garante **consistência eventual** e rastreabilidade.

## Consequências

### Positivas
- Operação de `authorize` torna-se segura sob retries.
- Publicação Kafka torna-se confiável e recuperável.
- Melhor auditabilidade (linha de outbox por evento).

### Negativas / Trade-offs
- A publicação do evento deixa de ser “imediata” (assíncrona).
- Complexidade adicional (tabela outbox + scheduler + estados).
- Necessidade de política de retry e tratamento de poison messages.

## Alternativas Consideradas

1. **Publicar direto no Kafka (sem Outbox):**
   - Rejeitado por risco de inconsistência e duplicidade sob falha.

2. **Transação distribuída (2PC):**
   - Rejeitado por complexidade, custo e não ser prática comum em Kafka.

3. **Constraint única em DB para idempotência:**
   - Válido, mas exige modelagem cuidadosa (por merchantId+orderId) e path de retorno.
   - Redis foi escolhido por simplicidade e performance.

## Detalhes de Implementação (referência)

### Idempotência
- Chave obrigatória: `Idempotency-Key`.
- Redis:
  - Key: `idem:{merchantId}:{idempotencyKey}` (exemplo)
  - Value: `paymentId`
  - TTL: ex.: 6h (ajustável).

### Outbox
- Tabela `outbox_event` (exemplo):
  - `id`, `aggregate_id`, `type`, `payload`, `status`, `created_at`, `sent_at`, `attempts`
- Scheduler publica e marca `SENT`.

## Métricas/Requisitos de Observabilidade

- Contadores:
  - `idempotency.hit`, `idempotency.miss`
  - `outbox.sent`, `outbox.retry`, `outbox.failed`
- Logs com `correlationId` em toda a jornada.
- Tracing distribuído (OTEL) recomendado.

## Referências
- Outbox Pattern (arquitetura de microsserviços)
- Práticas de idempotência em APIs de pagamento
