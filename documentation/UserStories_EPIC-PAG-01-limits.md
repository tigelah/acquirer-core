# EPIC: PAG-01 — Pagamentos com Limite e Crédito

## Objetivo: Garantir controle de crédito e resposta correta para compras sem saldo.

--- 

#### Story01: Simular compra com limite insuficiente

#### Como emissor

- Quero retornar erro insufficient_funds

- Para impedir transações acima do limite

#### Critérios de Aceite

- Compra acima do limite deve ser negada

- Mensagem padronizada do emissor aplicada

---

#### Story02: Configurar limite por usuário/cartão

- Permitir definição de limite individual

- Aplicável por PAN ou userId

#### Critérios

- Limite consultável via API

- Persistido no ledger/config store

---

#### Story03: Implementar limite diário e mensal

- Controlar gasto acumulado por período

#### Critérios

- Janela diária e mensal configurável

- Reset automático

---

#### Story04 : Consumir limite somente após captura

- Autorização não consome limite

- Captura efetiva reduz crédito

#### Critérios

- Autorização → reserva

- Captura → consumo real