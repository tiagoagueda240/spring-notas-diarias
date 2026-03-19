# 18 - Spring Web MVC e REST completo

## O que é Spring MVC (Servlet stack)

Spring MVC é o módulo web tradicional do Spring para apps HTTP no modelo request/response.

## Fluxo de uma requisição

1. Pedido entra no servidor
2. DispatcherServlet recebe
3. Encontra Controller e método
4. Faz binding/validação
5. Executa service
6. Gera resposta HTTP

## Anotações principais

- `@RestController`
- `@RequestMapping`
- `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`
- `@RequestBody`
- `@PathVariable`
- `@RequestParam`
- `@ResponseStatus`

## Binding e validação

- `@Valid` ativa validação
- Erros de validação normalmente devolvem `400`
- Ideal: tratamento global com `@ControllerAdvice`

## Serialização JSON

Spring usa Jackson para converter objetos Java em JSON.

## CORS

CORS define quais origens podem chamar a API no browser.

## Cliente HTTP no ecossistema Spring

- `RestClient` / `WebClient` para consumir APIs externas.

## Erros comuns

- Devolver entidades JPA diretamente.
- Não paginar endpoints de listagem.
- Misturar lógica de negócio no controller.

## Resumo de 30 segundos

Spring MVC é a base mais comum para REST API: controllers finos, services fortes, respostas HTTP corretas.
