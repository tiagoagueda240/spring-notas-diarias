# 08 - Configuração e Profiles

## O que é

Configuração Spring vive em `application.yaml` e variáveis de ambiente.

## Onde está no teu projeto

- `src/main/resources/application.yaml`

## O que já está bom

- Uso de variáveis de ambiente para segredos (`DB_PASSWORD`, `JWT_SECRET_KEY`, `GEMINI_API_KEY`).
- `open-in-view: false` (boa prática de performance/arquitetura).
- Actuator e Prometheus habilitados.

## Próximo passo recomendado

Adicionar perfis:

- `application-dev.yaml`
- `application-test.yaml`
- `application-prod.yaml`

Assim separas configurações por ambiente sem confusão.
