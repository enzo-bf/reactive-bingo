# Reactive Bingo API

API reativa de Bingo para portfólio e avaliação técnica, inspirada no desafio da DIO. Toda a camada HTTP e de persistência é não bloqueante, usando Spring WebFlux, Project Reactor e MongoDB Reactive.

## Visão geral

O sistema gerencia jogadores, abre rodadas, gera cartelas de 20 números (0 a 99), sorteia números sem repetição e finaliza automaticamente a rodada quando uma cartela é completada. O envio de e-mail é simulado por logs, sem provedor externo.

Fluxo principal:

1. Cadastrar jogadores.
2. Abrir uma rodada (`OPEN`).
3. Gerar uma cartela por jogador.
4. Sortear números até alguém completar a cartela.
5. A API marca a rodada como `FINISHED`, registra o vencedor e registra os e-mails em log.

## Tecnologias

- Java 21
- Spring Boot 4
- Spring WebFlux
- Project Reactor
- Spring Data MongoDB Reactive
- Maven
- JUnit 5, Mockito, StepVerifier e WebTestClient
- Lombok
- Bean Validation
- springdoc-openapi (Swagger UI)
- Docker e Docker Compose

## Como executar

### Pré-requisitos

- JDK 21+
- Docker (recomendado para o MongoDB)
- Maven Wrapper (`mvnw.cmd` no Windows)

### 1. Subir o MongoDB

```bash
docker compose up -d mongodb
```

Ou, para API + banco:

```bash
docker compose up --build
```

### 2. Executar a aplicação localmente

```bash
./mvnw spring-boot:run
```

No Windows:

```bat
mvnw.cmd spring-boot:run
```

A API sobe em `http://localhost:8080`.

MongoDB padrão: `mongodb://localhost:27017/reactive-bingo`.

### 3. Testes

```bash
./mvnw test
```

### 4. Documentação interativa

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

### 5. Postman

Importe `postman/ReactiveBingo.postman_collection.json`.

## Estrutura do projeto

```
src/main/java/com/enzobf/reactivebingo
├── controller     # Endpoints reativos
├── service        # Regras de negócio e e-mail mockado
├── repository     # ReactiveMongoRepository
├── domain         # Player, Round, BingoCard, Winner
├── dto            # Contratos de entrada e saída
├── mapper         # Conversão entity <-> DTO
├── exception      # Erros de negócio e handler global
├── config         # OpenAPI e índices MongoDB
└── util           # Geração de cartelas e números
```

## Endpoints

### Jogadores

| Método | Caminho | Descrição |
| --- | --- | --- |
| POST | `/players` | Cria jogador |
| GET | `/players` | Lista jogadores |
| GET | `/players/{id}` | Busca por id |
| PUT | `/players/{id}` | Atualiza jogador |
| DELETE | `/players/{id}` | Remove jogador |

### Rodadas

| Método | Caminho | Descrição |
| --- | --- | --- |
| POST | `/rounds` | Abre rodada |
| GET | `/rounds` | Lista rodadas |
| GET | `/rounds/{id}` | Busca rodada |
| POST | `/rounds/{roundId}/bingo-card/{playerId}` | Gera cartela |
| POST | `/rounds/{id}/generate-number` | Sorteia número |
| GET | `/rounds/{id}/current-number` | Número atual |

## Exemplos JSON

### POST `/players`

```json
{
  "name": "Enzo",
  "email": "enzo@email.com"
}
```

Resposta `201`:

```json
{
  "id": "66f1...",
  "name": "Enzo",
  "email": "enzo@email.com",
  "createdAt": "2026-10-23T12:00:00Z",
  "updatedAt": null
}
```

### POST `/rounds/{roundId}/bingo-card/{playerId}`

```json
{
  "id": "card-uuid",
  "playerId": "66f1...",
  "playerName": "Enzo",
  "playerEmail": "enzo@email.com",
  "numbers": [3, 8, 14, 21, 27, 33, 40, 45, 51, 58, 62, 70, 73, 81, 84, 88, 91, 93, 96, 99]
}
```

### POST `/rounds/{id}/generate-number`

```json
{
  "currentNumber": 21,
  "drawnNumbers": [8, 45, 21],
  "status": "OPEN",
  "winner": null
}
```

Quando a cartela é completada:

```json
{
  "currentNumber": 73,
  "drawnNumbers": [8, 45, 21, 73],
  "status": "FINISHED",
  "winner": {
    "playerId": "66f1...",
    "playerName": "Enzo",
    "playerEmail": "enzo@email.com",
    "bingoCardId": "card-uuid"
  }
}
```

## Regras de negócio

- Cartela com exatamente 20 números únicos entre 0 e 99.
- Um jogador só pode ter uma cartela por rodada.
- Cartelas não podem ser geradas depois do primeiro sorteio.
- Números sorteados não se repetem.
- Se alguma cartela tiver todos os números no histórico de sorteios, a rodada fecha sozinha.
- Novos sorteios são bloqueados em rodada `FINISHED`.
- `EmailService` apenas registra logs do vencedor e dos demais participantes.

## Docker

```bash
docker compose up --build
```

Serviços:

- `mongodb` na porta `27017`
- `app` na porta `8080`

## Checklist do desafio

- [x] CRUD reativo de jogadores
- [x] Rodadas OPEN/FINISHED
- [x] Cartelas com 20 números (0-99)
- [x] Sorteio sem repetição e histórico
- [x] Detecção automática de vencedor
- [x] E-mail mockado via log
- [x] MongoDB Reactive
- [x] Testes de services e controllers
- [x] README, Postman e OpenAPI
- [x] Dockerfile e docker-compose
