# 23 - Spring Testing completo

## Pirâmide de testes (modelo prático)

1. Unit tests (rápidos, muitos)
2. Slice tests (`@WebMvcTest`, `@DataJpaTest`)
3. Integration tests (`@SpringBootTest`)

## Ferramentas principais

- JUnit 5
- Mockito
- AssertJ
- MockMvc
- TestContext Framework

## Tipos de teste no ecossistema Spring

### Unit test
Testa classe isolada com mocks.

### Web slice test
Testa controller e contrato HTTP sem subir tudo.

### Data slice test
Testa repositórios com contexto JPA.

### Full integration test
Sobe aplicação completa para validar integração entre camadas.

## Boas práticas

- Nome de teste deve explicar comportamento.
- Um teste, uma responsabilidade.
- Evitar fragilidade por dependência externa.
- Preferir dados de teste claros e pequenos.

## Anti-padrões

- Testes grandes e difíceis de manter.
- Depender de ordem de execução.
- Misturar assert de muitos cenários no mesmo teste.

## Resumo de 30 segundos

Testes no Spring devem equilibrar velocidade e confiança: unit + slices + integração.
