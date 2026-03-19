# 12 - Anotações do Spring e JPA explicadas

Este ficheiro é um dicionário de `@anotações` usadas no teu projeto, em linguagem simples.

## 1) Anotações de arranque e configuração

### `@SpringBootApplication`

- O que faz: marca a classe principal da aplicação.
- Onde no projeto: `Application.java`.
- Efeito: liga auto-configuração, scan de componentes e contexto Spring.

### `@Configuration`

- O que faz: diz ao Spring que a classe define configurações (beans, regras, etc.).
- Onde no projeto: `SecurityConfig`, `ApplicationConfig`, `WebConfig`, `OpenApiConfig`.

### `@Bean`

- O que faz: regista manualmente um objeto no contexto do Spring.
- Exemplo no projeto: `PasswordEncoder`, `AuthenticationManager`, `SecurityFilterChain`.

### `@Value(...)`

- O que faz: injeta valor de configuração (`application.yaml` ou variável de ambiente).
- Exemplo no projeto: `JwtService` lê `application.security.jwt.secret-key`.

## 2) Anotações de API REST

### `@RestController`

- O que faz: classe que recebe pedidos HTTP e devolve JSON.
- Onde: `AuthenticationController`, `DailyEntryController`.

### `@RequestMapping`

- O que faz: define rota base da classe (ex: `/api/v1/auth`).

### `@PostMapping`, `@GetMapping`, `@DeleteMapping`

- O que fazem: definem método HTTP da rota (`POST`, `GET`, `DELETE`).

### `@RequestBody`

- O que faz: lê o corpo JSON do pedido e converte para um objeto Java.

### `@PathVariable`

- O que faz: lê valor da URL, ex: `/daily-entries/{id}`.

### `@RequestParam`

- O que faz: lê query params, ex: `?page=0&size=10`.

### `@AuthenticationPrincipal`

- O que faz: injeta utilizador autenticado no método do controller.
- No teu projeto: injeta `AppUser` após validação do JWT.

## 3) Anotações de validação (Jakarta Validation)

### `@Valid`

- O que faz: ativa validação de DTO antes da execução do método.

### `@NotBlank`

- O que faz: campo string não pode ser vazio ou só espaços.

### `@Email`

- O que faz: verifica formato básico de email.

### `@Size(min = X)`

- O que faz: define tamanho mínimo/máximo de texto.

## 4) Anotações de segurança Spring

### `@EnableWebSecurity`

- O que faz: ativa segurança web com Spring Security.

### `@Component`

- O que faz: regista classe como bean gerido pelo Spring.
- No teu projeto: `JwtAuthenticationFilter`.

### `@NonNull`

- O que faz: marca parâmetros como não nulos no filtro JWT.

## 5) Anotações de Service e Repository

### `@Service`

- O que faz: marca classe de lógica de negócio.
- Onde: `AuthenticationService`, `DailyEntryService`, `JwtService`.

### `@Repository`

- O que faz: marca camada de acesso a dados.
- Onde: repositórios JPA.

### `@EntityGraph`

- O que faz: carrega relações específicas numa query para evitar problema N+1.
- Onde: `DailyEntryRepository`.

## 6) Anotações JPA (base de dados)

### `@Entity`

- O que faz: transforma classe numa tabela da base de dados.

### `@Table(name = "...")`

- O que faz: define nome da tabela.

### `@Id`

- O que faz: marca chave primária.

### `@GeneratedValue(...)`

- O que faz: define geração automática da chave.

### `@Column(...)`

- O que faz: configura coluna (nullable, unique, tipo, etc.).

### `@OneToMany`

- O que faz: relação 1 para muitos.

### `@ManyToOne`

- O que faz: relação muitos para 1.

### `@ManyToMany`

- O que faz: relação muitos para muitos.

### `@JoinColumn`

- O que faz: define coluna FK em relação.

### `@JoinTable`

- O que faz: define tabela de ligação numa relação `ManyToMany`.

## 7) Anotações de exceções e docs

### `@ControllerAdvice`

- O que faz: captura erros globalmente na API.

### `@ExceptionHandler`

- O que faz: trata exceções específicas e converte para resposta HTTP.

### `@OpenAPIDefinition`, `@SecurityScheme`, `@SecurityRequirement`, `@Info`, `@Contact`

- O que fazem: documentam API no Swagger/OpenAPI.

### `@Tag`

- O que faz: agrupa endpoints por tema no Swagger UI.
- Exemplo no projeto: controllers de `Autenticação` e `Diário`.

### `@Operation`

- O que faz: adiciona título e descrição de cada endpoint.
- Vantagem: o Swagger deixa de mostrar só o nome técnico do método.

### `@ApiResponses`

- O que faz: define a lista de respostas possíveis de um endpoint.
- Normalmente usado com vários `@ApiResponse`.

### `@ApiResponse`

- O que faz: documenta um código HTTP específico (ex: `200`, `400`, `401`, `404`, `409`).
- Boa prática: documentar respostas de sucesso e os erros mais prováveis.

### `@Schema`

- O que faz: documenta campos de DTO (descrição, exemplo, formato).
- Resultado: os modelos no Swagger ficam claros para frontend e para estudo.

### `@Content`

- O que faz: define o conteúdo associado a um `@ApiResponse`.
- Uso comum: ligar códigos de erro ao modelo `ErrorResponse`.

### Que response codes usar (regra prática neste projeto)

- Usa sempre: `200/201/204`, `400`, `401`, `404`, `409` (quando aplicável) e `500`.
- `403` só quando tiveres autorização por papéis/permissões (roles).
- `503` e `504` normalmente dependem de infraestrutura/dependências e não precisam de ser documentados em todos os controllers.
- Mantém `@Operation` em todos os endpoints para Swagger ficar compreensível para equipas júnior.

### Onde já aplicámos no projeto

- `AuthenticationController`: `register` e `authenticate` com `@Operation` + `@ApiResponses`.
- `DailyEntryController`: create/list/get/delete com `@Operation` + `@ApiResponses`.

## 8) Anotações Lombok (reduzir código repetido)

Estas anotações não são do Spring, mas são muito usadas em projetos Spring para remover boilerplate.

### `@Getter`

- O que faz: gera todos os métodos `getX()` automaticamente.

### `@Setter`

- O que faz: gera todos os métodos `setX(...)` automaticamente.

### `@NoArgsConstructor`

- O que faz: gera construtor sem argumentos.
- Importante para JPA: entidades normalmente precisam deste construtor.

### `@AllArgsConstructor`

- O que faz: gera construtor com todos os campos.

### `@Builder`

- O que faz: cria padrão Builder para construir objetos de forma legível.
- Exemplo: `Tag.builder().name("Backend").build()`.

### `@Builder.Default`

- O que faz: garante que valores default definidos no campo continuam a ser aplicados quando usas `builder()`.
- Exemplo comum: listas iniciadas com `new ArrayList<>()`.

### `@RequiredArgsConstructor`

- O que faz: gera construtor automaticamente com todos os campos `final`.
- Uso típico em Spring: injeção de dependências por construtor sem escrever construtor manual.
- Onde já aplicámos no projeto: `AuthenticationController`, `AuthenticationService`, `DailyEntryController`, `SecurityConfig`, `ApplicationConfig`, `JwtAuthenticationFilter`.

## 9) Como usar Lombok com segurança

- Bom uso: entidades/modelos com muito boilerplate de getters/setters/construtores.
- Cuidado: não usar `@Data` em entidades JPA sem pensar em `equals/hashCode` e ciclos de relação.
- Regra prática: começa com `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor` e `@Builder`.
- Para classes de Spring (controller/service/config), usa `@RequiredArgsConstructor` com campos `final`.

## Resumo rápido para decorar

Se tiveres pouco tempo, memoriza primeiro:

- `@RestController`, `@Service`, `@Repository`, `@Entity`
- `@Valid`, `@NotBlank`, `@Email`
- `@ControllerAdvice`, `@ExceptionHandler`
- `@EnableWebSecurity`, `@AuthenticationPrincipal`
- `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`
- `@RequiredArgsConstructor`
