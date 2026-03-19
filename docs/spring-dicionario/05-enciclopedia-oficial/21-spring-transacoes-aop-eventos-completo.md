# 21 - Spring Transações, AOP e eventos completo

## Gestão de transações no Spring

Spring oferece modelo consistente de transações para JDBC, JPA e outras tecnologias.

## Duas abordagens

1. Declarativa com `@Transactional`
2. Programática com `TransactionTemplate`

## `@Transactional` (regras essenciais)

- Abre transação no método
- Commit no sucesso
- Rollback em exceções (normalmente runtime)

Configurações úteis:
- `readOnly = true`
- `propagation`
- `isolation`
- `timeout`

## Quando usar `TransactionTemplate`

- Fluxos especiais
- Controlo manual de fronteira transacional
- Cenários em que queres reduzir tempo de transação

## AOP e transações

Transações declarativas usam proxies AOP por trás.

Regra importante:
- chamada interna na mesma classe pode não acionar proxy como esperas.

## Eventos no Spring

- Publicar: `ApplicationEventPublisher`
- Ouvir: `@EventListener`
- Integrar com transação: listeners após commit

## Erros comuns

- Transação longa envolvendo chamadas externas (HTTP/IA).
- Usar transação em tudo sem necessidade.
- Esquecer comportamento de rollback.

## Resumo de 30 segundos

Transações garantem consistência. Mantém curta a janela transacional e evita I/O externo dentro dela.
