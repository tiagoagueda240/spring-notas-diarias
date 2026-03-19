# 00 - Começa por aqui

Se és júnior e sentes que “não percebes nada de Spring”, este é o ponto certo para começar.

## O que é Spring Boot (explicação simples)

Spring Boot é uma forma rápida de criar aplicações Java com:

- API HTTP (endpoints)
- ligação à base de dados
- autenticação e segurança
- validações e tratamento de erros

Ele faz muita configuração automática para tu não teres de escrever tudo à mão.

## Como pensar no projeto (modelo mental)

Pensa no pedido HTTP como uma viagem:

1. Cliente envia pedido (`POST`, `GET`, `DELETE`, ...)
2. Controller recebe
3. Service decide regra de negócio
4. Repository fala com base de dados
5. Resposta volta ao cliente com código HTTP

## O mínimo que precisas de decorar

- `@RestController`: entrada da API
- `@Service`: regras de negócio
- `@Repository`: acesso a dados
- `@Entity`: tabela da base de dados
- DTO: objeto para entrada/saída da API
- `@Valid`: valida dados recebidos

## Ordem de estudo recomendada (muito importante)

1. `01-arquitetura-camadas.md`
2. `02-rest-dto-validacao.md`
3. `03-service-transacoes.md`
4. `04-jpa-entidades-relacionamentos.md`
5. `06-seguranca-jwt.md`
6. `07-exception-handler.md`
7. `13-codigos-http-da-api-explicados.md`
8. `12-anotacoes-do-spring-e-jpa-explicadas.md`

## Erros normais de quem está a começar

- Meter lógica de negócio no Controller
- Devolver `Entity` diretamente em vez de DTO
- Não validar dados com `@Valid`
- Ignorar códigos HTTP corretos
- Fazer transações longas com chamadas externas

Estes erros são normais. O objetivo deste dicionário é evitar exatamente isso.
