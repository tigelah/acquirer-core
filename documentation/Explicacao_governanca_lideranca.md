

# Estamos seguindo a ISO 8583?


Sim, 

Não implementamos o protocolo ISO 8583 literalmente (bitmaps, MTI, campos numéricos),
mas **seguimos exatamente os mesmos conceitos financeiros**, usando uma arquitetura moderna,
event-driven e auditável.

Essa é a abordagem usada hoje por plataformas de pagamento modernas.

---

## Explicando de forma simples

A ISO 8583 define **como** mensagens de pagamento são trocadas entre adquirente, bandeira e emissor.
Ela nasceu em um contexto **legado, síncrono e altamente acoplado**.

Neste projeto, fizemos algo diferente:

> Mantivemos o significado financeiro da ISO 8583  
> e trocamos o formato antigo por eventos modernos.

---

## O que mudou na prática

Em vez de uma mensagem gigante e síncrona, temos eventos claros:

- `payment.authorize.requested`
- `payment.authorized` / `payment.declined`
- `payment.captured`
- `settlement.completed`

Cada evento carrega:
- valor,
- moeda,
- identificadores,
- correlação,
- contexto do pagamento.

Ou seja: **a mesma informação da ISO 8583, mas de forma mais legível, segura e escalável.**

---

## Segurança e governança

- PAN **nunca circula em claro**
- Usamos:
    - `panHash` para identificação
    - `panLast4` apenas para exibição
- Tudo é rastreável por `paymentId` e `correlationId`
- Cada movimentação financeira vira um evento imutável no Ledger

Isso melhora:
- auditoria
- reconciliação
- chargeback
- replay de eventos

---

## Por que não usar ISO 8583 direto?

Porque no core do sistema ele:
- dificulta evolução
- é ruim de observar
- não escala bem com microserviços

Se um dia precisarmos falar ISO 8583:
- fazemos isso **na borda**, com um adapter
- o core continua limpo e estável

---

## Para liderança e stakeholders

> Nosso sistema segue os mesmos princípios financeiros da ISO 8583,
> mas com uma arquitetura mais moderna, segura e preparada para crescer.

ISO 8583 não foi ignorado —  
ele foi **traduzido para uma linguagem atual**.