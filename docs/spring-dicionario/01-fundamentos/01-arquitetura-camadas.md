# 01 - Arquitetura em camadas

## O que é

Arquitetura em camadas organiza o código por responsabilidade:

- **Controller**: recebe pedido HTTP e devolve resposta.
- **Service**: regra de negócio.
- **Repository**: acesso à base de dados.
- **Entity/DTO**: dados persistidos e dados transportados.

## Onde está no teu projeto

- Controller: `journal/DailyEntryController.java`
- Service: `journal/DailyEntryService.java`
- Repository: `journal/repository/DailyEntryRepository.java`
- Entity: `journal/entity/DailyEntry.java`
- DTO: `journal/dto/DailyEntryDTO.java`

## Fluxo mental (simples)

Frontend → Controller → Service → Repository → Base de Dados → Service → Controller → Frontend.

## Regra de ouro

Controller não deve conter lógica pesada.
Lógica de negócio deve viver no Service.
