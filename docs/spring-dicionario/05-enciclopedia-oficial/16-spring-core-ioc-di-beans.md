# 16 - Spring Core completo: IoC, DI e Beans

## O que é IoC (Inversion of Control)

IoC significa que o controlo de criação dos objetos sai das tuas classes e passa para o container Spring.

Sem Spring:
- Tu fazes `new MinhaClasse()`.

Com Spring:
- O Spring cria e liga as dependências automaticamente.

## O que é DI (Dependency Injection)

DI é a técnica usada pelo Spring para injetar dependências.

Formas:
- Injeção por construtor (recomendada)
- Injeção por setter
- Injeção por campo (evitar em código novo)

## O que é um Bean

Bean é qualquer objeto gerido pelo container Spring.

Como um Bean nasce:
- `@Component`, `@Service`, `@Repository`, `@Controller`
- ou método `@Bean` numa classe `@Configuration`

## Ciclo de vida do Bean (versão simples)

1. Spring encontra a classe
2. cria instância
3. injeta dependências
4. bean fica disponível no contexto
5. no shutdown, bean é destruído

## Conceitos importantes

### Escopos
- Singleton (default): 1 instância por contexto.
- Prototype: nova instância por pedido ao container.
- Web scopes (request/session) em aplicações web.

### Qualifier e Primary
- `@Primary`: bean preferido quando há mais de um tipo.
- `@Qualifier`: escolhe bean específico pelo nome.

### Profiles
- `@Profile("dev")`, `@Profile("prod")` para ativar beans por ambiente.

## AOP em 1 minuto

AOP permite aplicar comportamento transversal sem poluir regras de negócio.
Exemplos:
- logs
- métricas
- transações (`@Transactional`)
- segurança

## Erros comuns

- Dependências circulares entre services.
- Bean duplicado sem `@Primary`/`@Qualifier`.
- Uso excessivo de campos estáticos para partilhar estado.

## Resumo de 30 segundos

Spring Core é o coração: container + beans + DI. Se dominares isso, o resto do Spring fica muito mais fácil.
