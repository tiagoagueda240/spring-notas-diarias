# 📘 Guia Definitivo: API de Diário Inteligente (Spring Boot + IA)

Bem-vindo ao projeto. Se és novo no mundo do Java e do Spring Boot, respira fundo. Este documento foi escrito especialmente para ti como um manual prático de estudo e desenvolvimento.

Este projeto é uma API RESTful de um Diário de Trabalho que utiliza Inteligência Artificial (Gemini) para ler os teus textos e extrair automaticamente as tarefas que realizaste, avaliando o impacto delas.

---

## 🧭 Modo Dicionário Spring (novo)

Para aprender de forma organizada (sem confusão e sem mexer no runtime da API), usa este guia por tópicos:

- **Índice principal**: `docs/spring-dicionario/README.md`

Capítulos:

Fundamentos:

- `docs/spring-dicionario/01-fundamentos/README.md`

Aplicação prática:

- `docs/spring-dicionario/02-aplicacao-pratica/README.md`

Testes:

- `docs/spring-dicionario/03-testes/README.md`

Referência rápida:

- `docs/spring-dicionario/04-referencia-rapida/README.md`

Enciclopédia Spring (base oficial, para estudo profundo):

- `docs/spring-dicionario/05-enciclopedia-oficial/README.md`

---

## 📖 Dicionário Spring (O Básico que precisas de saber)

O Spring Boot usa anotações (palavras que começam com `@`) para dar "poderes" às classes. Aqui estão as principais:

- **Injeção de Dependências (DI):** Em vez de fazeres `new MinhaClasse()`, tu pedes ao Spring: _"Ei, preciso desta classe"_. O Spring cria-a e entrega-ta no construtor. Chamamos a essas classes geridas pelo Spring de **Beans**.
- **`@RestController` (A Receção):** As classes com esta anotação ficam à escuta de pedidos HTTP (GET, POST). Elas recebem os dados do frontend (Angular) e passam-nos para a camada seguinte.
- **`@Service` (O Cérebro):** É onde vive a "Lógica de Negócio". É aqui que fazes validações complexas, chamas a IA e calculas coisas. É o coração do teu projeto.
- **`@Repository` (O Arquivista):** O único trabalho desta classe é falar com a Base de Dados usando o _Spring Data JPA_ (que gera as queries SQL automaticamente por ti).
- **`@Entity` (O Molde da BD):** Representa uma tabela na tua base de dados. Cada atributo desta classe será uma coluna na tabela.
- **DTO (Data Transfer Object):** O "carteiro". Nunca enviamos as nossas `@Entity` diretamente para o frontend por questões de segurança. Usamos classes `Record` do Java (DTOs) para transportar apenas a informação necessária.

---

## 🏛️ A Arquitetura em Camadas (O Fluxo de Dados)

Quando o frontend (Angular) envia um pedido para guardar um novo diário, o caminho é este:

1. **Angular** faz um pedido `POST /api/v1/daily-entries`.
2. O **`DailyEntryController`** recebe o pedido, valida se o Token JWT do utilizador está correto e passa o texto para o...
3. **`DailyEntryService`**. O serviço grava o texto original na BD, liga à IA da Google (Gemini) para extrair as tarefas e, quando a IA responde, grava as tarefas extraídas. Para gravar, pede ajuda ao...
4. **`DailyEntryRepository`**, que traduz as ações para SQL e guarda tudo em segurança na **Base de Dados**.
5. O percurso inverte-se: o Service transforma as Entidades da BD num **DTO** limpo e o Controller devolve esses dados em formato JSON de volta ao Angular.

---

## 📂 Estrutura de Pastas

- **`core`**: O escudo da API. Contém o `WebConfig` (CORS para permitir pedidos do Angular) e o `GlobalExceptionHandler` (apanha os erros e devolve mensagens formatadas em JSON em vez de estoirar o servidor).
- **`security`**: Os seguranças. Contém o `SecurityConfig` (define rotas públicas vs privadas), o `JwtService` (gera e lê os tokens) e o `JwtAuthenticationFilter` (o segurança à porta que valida o token em cada pedido HTTP).
- **`user`**: A entidade do utilizador (`AppUser`) e o seu acesso à Base de Dados.
- **`auth`**: Controladores e DTOs exclusivos para Registar novas contas e fazer Login.
- **`journal`**: Onde vive a Lógica do Diário. Entidades (`DailyEntry`, `TaskLog`, `Tag`), DTOs e o `DailyEntryService` (onde está a integração rápida e segura com a IA do Gemini).

---

## 🚀 Como Correr o Projeto Localmente

### 1. Pré-requisitos

- Java 17 ou superior instalado.
- Base de Dados (ex: PostgreSQL ou MySQL) a correr no teu computador.
- Chave de API da Google (Gemini AI).

### 2. Configuração do `application.yml`

Cria ou edita o ficheiro `src/main/resources/application.yml` com as tuas credenciais (Aviso: nunca faças commit das tuas passwords reais para o GitHub!):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/diario_db
    username: o_teu_user
    password: a_tua_password
  jpa:
    hibernate:
      ddl-auto: update # Cria/Atualiza as tabelas na BD automaticamente
    show-sql: true # Mostra o SQL gerado na consola (ótimo para estudar)
  ai:
    vertex:
      ai:
        gemini:
          api-key: COLA_AQUI_A_TUA_CHAVE_GEMINI_AI

application:
  security:
    jwt:
      # Gera uma string longa e aleatória (pelo menos 256-bit) para assinar os teus tokens
      secret-key: a_tua_chave_secreta_muito_longa_e_segura_colocada_aqui_sem_espacos
```

### 3. Iniciar a Aplicação

Corre a aplicação através do teu IDE (IntelliJ, Eclipse, VSCode) clicando no botão "Run" na classe principal (a que tem a anotação @SpringBootApplication).
