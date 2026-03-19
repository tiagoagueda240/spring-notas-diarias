# 03 - Service + Transações

## O que é

Service é a camada da regra de negócio.
Transação garante consistência: ou grava tudo, ou não grava nada.

## Onde está no teu projeto

- `journal/DailyEntryService.java`

## O que já está muito bem feito

No método `saveEntry`:

- Primeiro grava o diário rapidamente.
- Depois chama a IA fora de transação longa.
- Só abre transação curta na gravação das tarefas com `TransactionTemplate`.

Isto evita prender ligação da base de dados durante chamada externa (boa prática real).

## Conceito importante

Chamada externa demorada (IA, HTTP) + transação longa = má performance.
O teu código já evita este erro.
