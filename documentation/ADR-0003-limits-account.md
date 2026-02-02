
# acquirer-core — Patch PAG-01 (Limites & Crédito) 

Evolução Arquitetural: Limites, Ledger e Eventos no Acquirer Simulator

## 1. Contexto

Durante a evolução do simulador de adquirência, surgiu a necessidade de suportar controle real de crédito (limites por usuário/cartão, consumo apenas na captura, chargeback/refund seguros).

Isso expôs um problema clássico em arquiteturas CRUD-centradas:

- ❌ Status no banco não é suficiente para representar verdade financeira.

Em sistemas de pagamento reais, o estado financeiro precisa ser derivado de eventos imutáveis, não de colunas mutáveis.

Por isso, evoluímos a arquitetura para:

- Event-driven

- Ledger como fonte de verdade

- CQRS (write via eventos, read via projeções)

## 2. Decisão Arquitetural

Adotamos as seguintes decisões:

- Introduzir contexto financeiro completo nos eventos

- accountId

- userId

- panHash (nunca PAN em claro)

- Manter o acquirer-core como orquestrador

- Não calcula limite

- Não decide crédito

- Apenas publica eventos ricos

- Delegar decisões financeiras ao Issuer/Ledger

- Autorização → reserva

- Captura → consumo real

- Ledger → cálculo de crédito disponível

- Persistência via Outbox Pattern

- Garantir atomicidade DB + Kafka

- Evitar “pagamento salvo sem evento”

## 3. Como fiz  a mudança (Engenharia)
   ### 3.1 Domain Layer

#### Alterações

##### Payment passou a carregar:

- accountId
- userId
- panHash

##### Por quê

- O domínio precisa carregar contexto, não regras externas
- O pagamento representa a intenção, não o cálculo financeiro

##### Boas práticas

- Nenhuma dependência de infra
- Nenhuma lógica de limite no domínio

### 3.2 Application Layer (Use Cases)

#### AuthorizePaymentUseCase

Passa a:

- Gerar panHash
- Atribuir accountId/userId
- Publicar evento com contexto completo

#### Importante

- Nenhuma lógica condicional de limite (if credit < amount)
- Nenhuma chamada síncrona ao Ledger
- Mantém o Single Responsibility Principle a nivel arquitetural

### 3.3 Infrastructure Layer (Adapters)

#### PaymentRepositoryAdapter

#### Adaptado para:

- Persistir novos campos
- Rehidratar corretamente o agregado

#### Cuidados

- Rehidratação explícita (via PaymentRehydrator)
- Sem lógica de negócio no adapter

### 3.4 Outbox Pattern

- Refatoração crítica
- Substituição de Map.of(...) por LinkedHashMap
- Map.of quebra com null
- Inclusão explícita do campo type no payload

Ganhos

Consumers (risk/issuer) não dependem do tópico
Reprocessamento seguro (Kafka replay)

### 3.5 Flyway/Liquibase (Banco de Dados)

#### Estratégia

- Migrations incrementais (V2__add_limits_context.sql)
- Nunca alterar migrations antigas
- Compatível com rollback e ambientes já rodando

### 3.6 Testes

Testes continuam 100% unitários
Infra mockada

#### Por quê

- Garantir que a evolução não quebra contratos

- Garantir que eventos carregam contexto correto

### 4. Ganhos 

- ✅ Auditabilidade total
- ✅ Replay de eventos
- ✅ Chargeback seguro
- ✅ Limites diários/mensais corretos
- ✅ Evolução sem refatorações destrutivas
- ✅ Compatível com múltiplos produtos (crédito, débito, BNPL)

### 5. Perdas / Trade-offs

- ❌ Mais código (ifs, eventos, DTOs)
- ❌ Curva de aprendizado maior
- ❌ Mais disciplina de engenharia
- ❌ Debug exige entender fluxo de eventos

#### Trade-off consciente de Arquitetura: pagamos complexidade estrutural para ganhar segurança financeira e escala organizacional.

### 6. Quando essa arquitetura faz sentido?

#### Use quando:

- Dinheiro real
- Limites, estornos, chargebacks
- Auditoria regulatória
- Times maior que 1 squad

#### Não use quando:

- CRUD simples
- MVP sem impacto financeiro
- Produto sem risco contábil


Este patch garante que os eventos publicados carreguem os campos necessários para o emissor aplicar limites:

## Campos adicionados
- `accountId` (UUID) — conta/cliente a ser debitada no ledger
- `userId` (string) — opcional (quando o merchant enviar)
- `panHash` (string) — SHA-256 do PAN (sem expor PAN no evento)

## Eventos afetados
- `payment.authorize.requested` (publicado pelo acquirer-core via outbox)
- `payment.captured` (publicado após captura)
