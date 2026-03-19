# 05 - Repositories e Queries

## O que é

Ao fazer `extends JpaRepository`, ganhas CRUD pronto sem SQL manual.

## Onde está no teu projeto

- `journal/repository/DailyEntryRepository.java`
- `journal/repository/TagRepository.java`
- `user/AppUserRepository.java`

## Query derivada por nome

Exemplo real:

- `findByAppUserOrderByEntryDateDesc(...)`

O Spring interpreta o nome do método e constrói a query.

## Otimização importante que já tens

`@EntityGraph(attributePaths = {"tasks", "tasks.tags"})`

Isto reduz problema N+1 em leitura paginada.
