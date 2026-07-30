# Docker

## Objetivo

Executar o backend Spring Boot e o PostgreSQL em containers, usando volumes persistentes para o banco de dados e para uploads de imagens de produtos.

## Arquivos

* `backend/Dockerfile`: cria a imagem do backend com build multi-stage.
* `backend/.dockerignore`: reduz o contexto enviado ao Docker.
* `docker-compose.yml`: orquestra backend, PostgreSQL, healthchecks e volumes.

## Imagem do backend

O Dockerfile usa dois estagios:

1. `maven:3.9.9-eclipse-temurin-21` para baixar dependencias e gerar o JAR, pois o projeto nao possui `mvnw`.
2. `eclipse-temurin:21-jre-alpine` para executar o JAR com Java 21.

O container expoe a porta `8080` por padrao e usa o endpoint publico `/api/health` no healthcheck. O Compose tambem declara healthcheck para o backend e para o PostgreSQL.

## Banco de dados

O Compose usa `postgres:16-alpine`.

O backend acessa o banco pelo hostname interno do Compose:

```text
jdbc:postgresql://postgres:5432/finopaladar
```

As migrations Flyway continuam sendo executadas pelo backend ao iniciar.

## Volumes

O Compose cria dois volumes nomeados:

* `postgres_data`: dados persistentes do PostgreSQL.
* `produto_uploads`: arquivos enviados para `/app/uploads/produtos` dentro do backend.

O caminho de upload permanece compativel com a configuracao existente:

```text
UPLOAD_IMAGE_DIR=uploads/produtos
UPLOAD_IMAGE_URL_PREFIX=/uploads/produtos
```

## Variaveis de ambiente

O `docker-compose.yml` possui valores padrao. Para sobrescrever, crie um arquivo `.env` na raiz do projeto.

Exemplo:

```env
SERVER_PORT=8080

DB_PORT=5432
DB_NAME=finopaladar
DB_USERNAME=postgres
DB_PASSWORD=postgres

DELIVERY_FEE=0.00
JWT_SECRET=change-this-secret-with-at-least-32-characters
JWT_EXPIRATION_MINUTES=120
WHATSAPP_PHONE_NUMBER=5583999999999

UPLOAD_IMAGE_DIR=uploads/produtos
UPLOAD_IMAGE_URL_PREFIX=/uploads/produtos
UPLOAD_IMAGE_MAX_SIZE_BYTES=5242880
UPLOAD_IMAGE_MAX_FILE_SIZE=5MB
UPLOAD_IMAGE_MAX_REQUEST_SIZE=5MB
```

No Compose, o backend sempre usa `SPRING_PROFILES_ACTIVE=prod` para carregar `application-prod.yml` e conectar ao PostgreSQL do container.

## Comandos

Executar build da imagem:

```bash
docker compose build
```

Validar a configuracao final do Compose:

```bash
docker compose config
```

Subir a aplicacao:

```bash
docker compose up -d
```

Ver logs:

```bash
docker compose logs -f backend
```

Encerrar os containers mantendo os volumes:

```bash
docker compose down
```

Encerrar e remover tambem os volumes persistentes:

```bash
docker compose down -v
```

## URLs

API:

```text
http://localhost:8080
```

Healthcheck:

```text
http://localhost:8080/api/health
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```
