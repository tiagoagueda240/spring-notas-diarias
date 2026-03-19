# 25 - Spring Integration: Caching, Scheduling, Async e Messaging

Este capítulo cobre a área de **Integration** da documentação oficial do Spring Framework.

## 1) Caching

### O que é

Caching guarda resultados em memória (ou outro storage) para evitar trabalho repetido.

### Anotações principais

- `@EnableCaching`
- `@Cacheable`
- `@CachePut`
- `@CacheEvict`

### Quando usar

- Leitura repetida de dados que mudam pouco.
- Operações pesadas de cálculo.

### Cuidado

- Cache desatualizada (stale data).
- Definir estratégia de invalidação.

## 2) Scheduling

### O que é

Executar tarefas automaticamente por tempo.

### Anotações principais

- `@EnableScheduling`
- `@Scheduled(fixedRate = ...)`
- `@Scheduled(cron = "...")`

### Quando usar

- Limpeza periódica.
- Reprocessamento automático.
- Jobs de manutenção.

### Cuidado

- Jobs longos podem sobrepor execução.
- Atenção em ambiente com múltiplas instâncias.

## 3) Async

### O que é

Executar métodos em background sem bloquear a thread principal.

### Anotações principais

- `@EnableAsync`
- `@Async`

### Quando usar

- Envio de email.
- Processos não críticos para resposta imediata.

### Cuidado

- Gestão de exceções assíncronas.
- Limites de thread pool.

## 4) Messaging

### O que é

Comunicação assíncrona por mensagens (fila/tópico).

### Tecnologias comuns

- JMS
- AMQP (RabbitMQ)
- Kafka (via projetos Spring específicos)

### Vantagens

- Desacoplamento entre serviços.
- Resiliência e escalabilidade.

## 5) Eventos internos Spring

### O que é

Publicar e consumir eventos dentro da própria aplicação.

### APIs comuns

- `ApplicationEventPublisher`
- `@EventListener`

### Quando usar

- Reagir a ações de domínio sem acoplar diretamente serviços.

## Resumo de 30 segundos

A secção Integration do Spring cobre padrões essenciais para produção: cache, tarefas agendadas, processamento async e comunicação por mensagens.
