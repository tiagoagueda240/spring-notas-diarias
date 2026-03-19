# 11 - Testes na prática (0 ao 100%)

Este capítulo mostra, de forma simples, como pensar testes Spring por camadas.

## Visão geral rápida

Criámos 5 testes organizados por camadas:

- Controller: `AuthenticationControllerWebMvcTest`
- Service: `AuthenticationServiceTest`
- Repository: `TagRepositoryDataJpaTest`
- Security (JWT): `JwtServiceTest`
- Exception Handling: `GlobalExceptionHandlerWebMvcTest`

Objetivo:

- Ver a diferença entre teste de web, lógica e base de dados.
- Ganhar confiança sem mexer no código de produção.

## 1) Controller test (camada HTTP)

Ficheiro:

- `src/test/java/com/tiagoagueda/api/auth/AuthenticationControllerWebMvcTest.java`

O que valida:

- Pedido válido no `/api/v1/auth/register` devolve `200` e token JSON.
- Pedido inválido devolve `400`.
- Em payload inválido, o service não é chamado.

Como pensar:

- Controller test não testa base de dados.
- Controller test valida contrato HTTP (status, JSON, validação).

## 2) Service test (regra de negócio)

Ficheiro:

- `src/test/java/com/tiagoagueda/api/auth/AuthenticationServiceTest.java`

O que valida:

- Se email já existe, lança `UserAlreadyExistsException`.
- Se registo é válido, password é codificada, utilizador é guardado e token é devolvido.

Como pensar:

- Service test testa decisões da regra de negócio.
- Dependências externas são mockadas (repository, encoder, jwt).

## 3) Repository test (acesso a dados)

Ficheiro:

- `src/test/java/com/tiagoagueda/api/journal/repository/TagRepositoryDataJpaTest.java`

O que valida:

- Método `findByNameIgnoreCase` encontra tag ignorando maiúsculas/minúsculas.

Como pensar:

- Repository test valida query e mapeamento JPA.
- Usa `@DataJpaTest` para subir apenas contexto de persistência.

## 4) Security test (JWT)

Ficheiro:

- `src/test/java/com/tiagoagueda/api/security/JwtServiceTest.java`

O que valida:

- Geração de token.
- Extração de username do token.
- Validação verdadeira para o dono do token.
- Validação falsa para outro utilizador.

Como pensar:

- Este é um teste unitário puro de segurança.
- Não sobe servidor, não chama base de dados.

## 5) Exception Handler test (erros HTTP)

Ficheiro:

- `src/test/java/com/tiagoagueda/api/core/exception/GlobalExceptionHandlerWebMvcTest.java`

O que valida:

- `BadCredentialsException` → 401
- `NoSuchElementException` → 404
- `UserAlreadyExistsException` → 409
- erro de validação `@Valid` → 400
- erro inesperado → 500

Como pensar:

- Garante consistência do contrato de erro da API.
- Evita regressões quando mexes no handler global.

## Porque adicionámos H2 no `pom.xml`

Para `@DataJpaTest` funcionar sem depender do PostgreSQL local, adicionámos:

- `com.h2database:h2` com scope `test`.

Assim os testes de repository podem correr em memória.

## Como correr os testes

No teu ambiente atual, o Maven não está instalado e o wrapper `.mvn/wrapper` está em falta.

Para resolver:

1. Instalar Maven globalmente, ou
2. Adicionar ao projeto a pasta `.mvn/wrapper` (ficheiros do Maven Wrapper).

Depois, correr:

- `mvn test` (se tiveres Maven global)
- ou `./mvnw test` (quando o wrapper estiver completo)

## Próximo nível (quando quiseres)

Para evoluir ainda mais além do 100% base:

- Adicionar teste de paginação no `DailyEntryRepository`.
- Adicionar teste de autorização em endpoint protegido (`/daily-entries`).
- Adicionar teste de integração completo com base de dados de teste.
