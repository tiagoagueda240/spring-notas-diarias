# 06 - Segurança JWT com Spring Security

## O que é

JWT permite autenticação stateless: cada pedido traz token.

## Onde está no teu projeto

- `security/SecurityConfig.java`
- `security/JwtAuthenticationFilter.java`
- `security/JwtService.java`
- `security/ApplicationConfig.java`
- `auth/AuthenticationService.java`

## Fluxo de autenticação

1. `POST /api/v1/auth/authenticate` valida email/password.
2. `JwtService` gera token.
3. Cliente envia `Authorization: Bearer <token>`.
4. `JwtAuthenticationFilter` valida token e autentica no `SecurityContext`.

## Coisas corretas já aplicadas

- `SessionCreationPolicy.STATELESS`
- Password com `BCryptPasswordEncoder`
- Rotas públicas e privadas bem definidas
