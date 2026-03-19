# 04 - JPA Entidades e Relacionamentos

## O que é

- `@Entity`: classe que vira tabela.
- `@Id` + `@GeneratedValue`: chave primária.
- Relacionamentos: `@OneToMany`, `@ManyToOne`, `@ManyToMany`.

## Onde está no teu projeto

- `journal/entity/DailyEntry.java`
- `journal/entity/TaskLog.java`
- `journal/entity/Tag.java`

## Mapa das relações

- 1 utilizador → N diários (`AppUser` ↔ `DailyEntry`)
- 1 diário → N tarefas (`DailyEntry` ↔ `TaskLog`)
- N tarefas ↔ N tags (`TaskLog` ↔ `Tag`)

## Boas práticas já aplicadas

- Uso de `FetchType.LAZY` para performance.
- Método auxiliar `addTask` para manter relação bidirecional consistente.
- `columnDefinition = "TEXT"` para textos longos.
