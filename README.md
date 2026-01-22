# acquirer-core

Serviço central do simulador de adquirência.  
É responsável por **gerenciar o ciclo de vida do pagamento**, desde a autorização até a liquidação, garantindo **consistência, idempotência e confiabilidade na comunicação por eventos**.

---

## Propósito do serviço

O `acquirer-core` representa o **núcleo da adquirente**, responsável por:

- Criar e gerenciar pagamentos
- Validar a veracidade do cartão (certificadora + bandeira)
- Orquestrar o fluxo de autorização, captura, clearing e settlement
- Publicar e consumir eventos Kafka
- Garantir **idempotência** e **entrega confiável de eventos** (Outbox Pattern)

---

## Idempotência (conceito fundamental)

### Por que precisamos de idempotência?

Em pagamentos, requisições podem ser reenviadas por:
- Timeout de rede
- Retry automático do merchant
- Falhas intermitentes
- Ações duplicadas do usuário

Sem idempotência, isso causa:
- Pagamentos duplicados
- Eventos duplicados
- Liquidação incorreta
- Chargebacks

### Como a idempotência funciona neste serviço

- O endpoint de autorização **exige** o header `Idempotency-Key`
- A chave é armazenada no Redis com TTL
- A chave é mapeada para um `paymentId`
- Requisições repetidas retornam **o mesmo pagamento**, sem efeitos colaterais

### Regras

- Sem `Idempotency-Key` → `400 Bad Request`
- Chave repetida → `200 OK` com o mesmo `paymentId`
- Nenhum evento Kafka é duplicado

---

## Endpoints HTTP

Base URL: `http://localhost:8081`

### 1) Autorizar pagamento
`POST /payments/authorize`

Headers:
- `Idempotency-Key` (obrigatório)

Respostas esperadas:
- **200 OK** com o pagamento (idempotente: mesma chave retorna o mesmo `paymentId`)
- **422 Unprocessable Entity** se a certificadora/bandeira rejeitar o cartão
- **400 Bad Request** se payload inválido

---

### 2) Capturar pagamento
`POST /payments/{paymentId}/capture`

Regras:
- Só permite captura se status for `AUTHORIZED`
- Publica evento de captura via **Outbox Pattern**

---

### 3) Consultar pagamento
`GET /payments/{paymentId}`

- Retorna status atual
- Retorna apenas dados não sensíveis (ex.: last4 do PAN)

---

## Health e métricas (Actuator)

- `GET /actuator/health`
- `GET /actuator/prometheus`

---

## Eventos Kafka

> Os nomes podem variar conforme configuração, abaixo é o padrão do simulador.

### Produzidos pelo acquirer-core (via Outbox)
- `payment.authorize.requested`
- `payment.capture.requested`

### Consumidos pelo acquirer-core
- `payment.risk.approved`
- `payment.risk.rejected`
- `payment.authorized`
- `payment.declined`
- `payment.captured`
- `settlement.completed` ✅ (marca pagamentos como `SETTLED`)

### Contratos importantes
Todos os eventos contêm:
- `eventId` (UUID)
- `occurredAt` (ISO-8601)
- `correlationId`

Evento de settlement:
```json
{
  "paymentIds": ["uuid-1", "uuid-2"]
}
```
### Outbox Pattern
Para evitar inconsistências do tipo “salvou no banco mas não publicou no Kafka”, o serviço utiliza Outbox Pattern.

Fluxo:
- `Caso de uso salva Payment`
- `Caso de uso salva OutboxEvent (mesma transação)`
- `Scheduler publica no Kafka`
- `Evento é marcado como SENT`
- `Retry/backoff em caso de falha`



### Tabela típica: outbox_event

Campos:id aggregate_id
type
payload
status
created_at
sent_at



### Como rodar
Variáveis de ambiente (exemplo)


```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/acquiring
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
REDIS_HOST=localhost
CARD_CERTIFIER_BASE_URL=http://localhost:8086
BRAND_NETWORK_BASE_URL=http://localhost:8087
```

###  Rodar com Docker Compose (recomendado)
```
docker compose up -d postgres redis kafka
docker compose up -d card-certifier brand-network-hub
docker compose up -d acquirer-core
```
Verificar:

```
curl http://localhost:8081/actuator/health
```
Rodar local (Maven)
```
cd acquirer-core
mvn clean spring-boot:run
```
Testes e cobertura

Rodar testes + JaCoCo
```
cd services/acquirer-core
mvn clean verify
```
Relatório:

target/site/jacoco/index.html

