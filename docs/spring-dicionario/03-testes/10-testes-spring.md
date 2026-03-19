# 10 - Testes Spring (o que falta aqui)

## Estado atual

Atualmente tens apenas teste de contexto:

- `src/test/java/com/tiagoagueda/api/ApplicationTests.java`

## Objetivo para ficar “dicionário completo”

Adicionar 3 níveis de testes:

1. **Controller** com `@WebMvcTest`

- Valida status HTTP, payload JSON e validações.

2. **Repository** com `@DataJpaTest`

- Valida queries derivadas e relacionamentos.

3. **Service** com `Mockito`

- Valida regra de negócio sem subir servidor completo.

## Regra simples

Começa por 1 teste por camada (não precisas 20 de uma vez).
Pequenos passos reduzem confusão.
