# 09 - OpenAPI/Swagger + Actuator

## O que é

- OpenAPI/Swagger: documentação viva dos endpoints.
- Actuator: saúde e métricas da app.

## Onde está no teu projeto

- `core/config/OpenApiConfig.java`
- `application.yaml` (bloco `management`)
- `security/SecurityConfig.java` (libertação de rotas docs/actuator)

## Endpoints úteis

- Swagger UI: `/swagger-ui/index.html`
- OpenAPI JSON: `/v3/api-docs`
- Health: `/actuator/health`
- Prometheus: `/actuator/prometheus`

## Por que isto é importante

Isto aproxima o projeto de práticas reais de produção e observabilidade.
