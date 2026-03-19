# 02 - REST Controllers + DTOs + Validação

## O que é

- `@RestController`: transforma retorno em JSON automaticamente.
- DTO: objeto para entrada/saída da API.
- `@Valid`: aplica validações antes de entrar no Service.

## Onde está no teu projeto

- `auth/AuthenticationController.java`
- `journal/DailyEntryController.java`
- `auth/dto/RegisterRequest.java`
- `journal/dto/DailyEntryRequest.java`

## Exemplo prático

No `DailyEntryController`, este padrão está correto:

- Recebe `@RequestBody` com `@Valid`.
- Usa `@AuthenticationPrincipal` para obter utilizador autenticado.
- Devolve `201 Created` ao criar recurso.

## Dica de júnior para sénior

Tudo o que entra pela API deve ser DTO validado.
Evita receber entidades JPA diretamente no Controller.
