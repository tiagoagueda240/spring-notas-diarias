# 17 - Spring Boot completo: auto-configuração, starters e propriedades

## O que o Spring Boot resolve

Spring Boot reduz configuração manual com:
- Auto-configuração
- Dependências "starter"
- Configuração por propriedades
- Arranque rápido da aplicação

## Auto-configuração

O Boot analisa o classpath e configura componentes automaticamente.

Exemplo mental:
- Se encontra Spring Web, prepara MVC.
- Se encontra Data JPA + datasource, configura JPA e transações.

## Starters

Starters são pacotes prontos de dependências.

Exemplos comuns:
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `spring-boot-starter-test`

## Configuração por propriedades

Ficheiros:
- `application.yaml`
- `application.properties`

Pode-se usar:
- valores fixos
- variáveis de ambiente
- perfis (`application-dev.yaml`, `application-prod.yaml`)

## Perfis

Perfis mudam comportamento por ambiente:
- dev
- test
- prod

Isto evita configurações perigosas em produção.

## Boas práticas

- Segredos sempre via variáveis de ambiente.
- Não usar `ddl-auto=update` em produção sem estratégia de migração.
- Separar configurações por perfil.

## Erros comuns

- Misturar configurações de dev e prod no mesmo ficheiro.
- Esquecer defaults seguros para variáveis críticas.

## Resumo de 30 segundos

Spring Boot acelera o setup. Menos configuração manual, mais foco em regra de negócio.
