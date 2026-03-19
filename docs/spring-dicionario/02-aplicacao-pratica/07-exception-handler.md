# 07 - Tratamento global de exceções

## O que é

`@ControllerAdvice` centraliza erros da API.

## Onde está no teu projeto

- `core/exception/GlobalExceptionHandler.java`
- `core/exception/ErrorResponse.java`

## O que já está implementado

- `BadCredentialsException` → `401 Unauthorized`
- `NoSuchElementException` → `404 Not Found`
- `MethodArgumentNotValidException` → `400 Bad Request`
- `UserAlreadyExistsException` → `409 Conflict`
- `Exception` genérica → `500 Internal Server Error`

## Benefício

Sem este padrão, cada controller teria `try/catch` repetido.
Com este padrão, respostas de erro ficam consistentes e limpas.
