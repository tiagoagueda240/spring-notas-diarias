# 13 - Códigos HTTP da API explicados

Este guia explica os códigos HTTP usados (ou esperados) no teu projeto.

## Regra simples para júnior

- `2xx` = sucesso
- `4xx` = erro do cliente (pedido inválido, sem permissões, etc.)
- `5xx` = erro do servidor

## Códigos usados no teu projeto

### `200 OK`

- Significa: pedido correu bem.
- Onde no projeto:
  - login/registo em `AuthenticationController`
  - listagens e leituras em controllers

### `201 Created`

- Significa: recurso foi criado.
- Onde no projeto:
  - criação de diário em `DailyEntryController#createEntry`.

### `204 No Content`

- Significa: ação bem sucedida sem corpo de resposta.
- Onde no projeto:
  - delete de diário em `DailyEntryController#deleteEntry`.

### `400 Bad Request`

- Significa: pedido inválido (normalmente validação de DTO falhou).
- Onde no projeto:
  - `GlobalExceptionHandler` para `MethodArgumentNotValidException`.

### `401 Unauthorized`

- Significa: falta autenticação ou credenciais inválidas.
- Onde no projeto:
  - login com credenciais erradas (`BadCredentialsException`).
  - acesso protegido sem token válido.

### `404 Not Found`

- Significa: recurso não encontrado.
- Onde no projeto:
  - `NoSuchElementException` tratada no `GlobalExceptionHandler`.

### `409 Conflict`

- Significa: conflito de dados (ex: email já existe).
- Onde no projeto:
  - `UserAlreadyExistsException` no registo.

### `500 Internal Server Error`

- Significa: erro inesperado no servidor.
- Onde no projeto:
  - fallback do `GlobalExceptionHandler`.

## Códigos que também deves conhecer

### `403 Forbidden`

- Significa: autenticado, mas sem permissão.
- Útil quando tiveres roles (`ADMIN`, `USER`).

### `405 Method Not Allowed`

- Significa: endpoint existe, mas método HTTP está errado.
- Exemplo: fazer `PUT` numa rota que só aceita `POST`.

### `415 Unsupported Media Type`

- Significa: payload com content-type errado.
- Exemplo: API espera JSON e cliente envia outro formato.

## Como escolher o código certo (checklist)

1. Foi criado recurso novo? → `201`
2. Foi sucesso sem resposta de corpo? → `204`
3. Foi sucesso normal? → `200`
4. Dados inválidos? → `400`
5. Sem autenticação/token inválido? → `401`
6. Recurso não existe? → `404`
7. Conflito de unicidade? → `409`
8. Erro inesperado no servidor? → `500`

## Dica final

Usar código HTTP certo deixa a API previsível para frontend e para testes.
