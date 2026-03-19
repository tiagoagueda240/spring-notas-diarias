# 19 - Spring WebFlux (reativo) completo

## O que é WebFlux

WebFlux é a stack reativa do Spring para aplicações não-bloqueantes.

## Quando usar

Usa WebFlux quando tens:
- muitas chamadas concorrentes
- I/O intensivo
- necessidade de throughput alto com menos threads

## Conceitos base

- Programação reativa (Publisher/Subscriber)
- `Mono<T>`: 0 ou 1 item
- `Flux<T>`: 0..N itens

## WebFlux vs MVC

- MVC: modelo tradicional, simples, muito usado.
- WebFlux: modelo reativo, ótimo para cenários específicos.

Não é obrigatório usar WebFlux em todos os projetos.

## Peças principais

- Controllers reativos
- Functional endpoints (`RouterFunction`)
- `WebClient` para chamadas HTTP reativas

## Cuidados importantes

- Evitar operações bloqueantes no fluxo reativo.
- Entender backpressure.
- Misturar blocante com reativo sem cuidado causa perda de performance.

## Erros comuns

- Escolher WebFlux só por moda.
- Usar `.block()` em pontos errados.

## Resumo de 30 segundos

WebFlux é poderoso para I/O concorrente. Para maioria de APIs CRUD, MVC continua excelente e mais simples.
