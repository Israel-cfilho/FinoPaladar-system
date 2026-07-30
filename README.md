# FinoPaladar System

Sistema web para gerenciamento de vendas de bolos de rolo. O projeto tem backend em Java 21 com Spring Boot, PostgreSQL, Flyway, JWT, API REST e documentacao OpenAPI/Swagger. O frontend em React + TypeScript faz parte do escopo do sistema, mas este repositorio atualmente contem a implementacao do backend.

## Sumario

* [Tecnologias](#tecnologias)
* [Estrutura](#estrutura)
* [Como instalar](#como-instalar)
* [Como configurar](#como-configurar)
* [Como executar](#como-executar)
* [Como testar](#como-testar)
* [Swagger](#swagger)
* [Docker](#docker)
* [Endpoints principais](#endpoints-principais)
* [Documentacao](#documentacao)

## Tecnologias

Backend:

* Java 21
* Spring Boot 3.3.5
* Spring Data JPA
* Spring Validation
* Spring Security
* JWT
* PostgreSQL
* Flyway
* Maven
* Springdoc OpenAPI

Infraestrutura:

* Docker
* Docker Compose
* PostgreSQL com volume persistente
* Volume persistente para uploads de produtos

## Estrutura

```text
.
+-- backend/
|   +-- src/main/java/br/com/finopaladar/backend/
|   +-- src/main/resources/
|   +-- src/test/java/br/com/finopaladar/backend/
|   +-- Dockerfile
|   +-- .dockerignore
|   +-- .env.example
|   +-- pom.xml
+-- docs/
+-- docker-compose.yml
+-- README.md
```

Arquitetura do backend:

```text
Controller
Service
Repository
Banco de Dados
```

Regras de negocio devem ficar na camada Service. Controllers devem retornar DTOs, nunca entidades JPA diretamente.

## Como instalar

### Requisitos locais

Instale:

* Java 21
* Maven 3.9 ou superior
* PostgreSQL 16 ou superior
* Docker e Docker Compose, caso queira executar por containers

O projeto nao possui Maven Wrapper (`mvnw`), entao o Maven precisa estar instalado no ambiente ou disponivel pela IDE.

### Baixar dependencias

No diretorio do backend:

```bash
cd backend
mvn dependency:go-offline
```

### Criar banco local

Crie um banco PostgreSQL para execucao local:

```sql
CREATE DATABASE finopaladar;
```

Por padrao, o perfil `local` usa:

```text
DB_HOST=localhost
DB_PORT=5432
DB_NAME=finopaladar
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

As tabelas sao criadas e versionadas por migrations Flyway em `backend/src/main/resources/db/migration`.

## Como configurar

As configuracoes ficam em:

* `backend/src/main/resources/application.yml`
* `backend/src/main/resources/application-local.yml`
* `backend/src/main/resources/application-test.yml`
* `backend/src/main/resources/application-prod.yml`
* `backend/.env.example`

### Perfis Spring

Perfil padrao:

```text
SPRING_PROFILES_ACTIVE=local
```

Perfis disponiveis:

* `local`: desenvolvimento local com PostgreSQL local.
* `test`: testes automatizados com banco de teste configuravel.
* `prod`: producao/container, usando `DATABASE_URL`.

### Variaveis principais

| Variavel | Padrao | Descricao |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `local` | Perfil ativo da aplicacao. |
| `SERVER_PORT` | `8080` | Porta HTTP do backend. |
| `DB_HOST` | `localhost` | Host do PostgreSQL no perfil local. |
| `DB_PORT` | `5432` | Porta do PostgreSQL local. |
| `DB_NAME` | `finopaladar` | Nome do banco local. |
| `DB_USERNAME` | `postgres` | Usuario do banco. |
| `DB_PASSWORD` | `postgres` | Senha do banco. |
| `DATABASE_URL` | sem padrao no `prod` | URL JDBC usada no perfil `prod`. |
| `JPA_SHOW_SQL` | `true` no local | Exibe SQL no console em desenvolvimento. |
| `DELIVERY_FEE` | `0.00` | Taxa de entrega usada no calculo de pedidos. |
| `JWT_SECRET` | segredo local | Chave para assinar tokens JWT. Use valor forte em producao. |
| `JWT_EXPIRATION_MINUTES` | `120` | Tempo de expiracao do token JWT. |
| `WHATSAPP_PHONE_NUMBER` | vazio | Numero usado para gerar link de WhatsApp. |
| `UPLOAD_IMAGE_DIR` | `uploads/produtos` | Diretorio onde imagens de produtos sao salvas. |
| `UPLOAD_IMAGE_URL_PREFIX` | `/uploads/produtos` | Prefixo publico das imagens. |
| `UPLOAD_IMAGE_MAX_SIZE_BYTES` | `5242880` | Tamanho maximo em bytes. |
| `UPLOAD_IMAGE_MAX_FILE_SIZE` | `5MB` | Limite multipart de arquivo. |
| `UPLOAD_IMAGE_MAX_REQUEST_SIZE` | `5MB` | Limite multipart da requisicao. |

### Configuracao via PowerShell

Exemplo para execucao local:

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
$env:DB_HOST="localhost"
$env:DB_PORT="5432"
$env:DB_NAME="finopaladar"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
$env:JWT_SECRET="change-this-secret-with-at-least-32-characters"
```

### Configuracao via Bash

Exemplo para execucao local:

```bash
export SPRING_PROFILES_ACTIVE=local
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=finopaladar
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=change-this-secret-with-at-least-32-characters
```

## Como executar

### Executar localmente com Maven

No diretorio `backend`:

```bash
mvn spring-boot:run
```

Aplicacao:

```text
http://localhost:8080
```

Healthcheck:

```text
http://localhost:8080/api/health
```

### Gerar JAR

No diretorio `backend`:

```bash
mvn clean package
```

Executar o JAR:

```bash
java -jar target/finopaladar-backend-0.0.1-SNAPSHOT.jar
```

### Executar com Docker Compose

Na raiz do projeto:

```bash
docker compose build
docker compose up -d
```

Ver logs:

```bash
docker compose logs -f backend
```

Encerrar mantendo os volumes:

```bash
docker compose down
```

Encerrar removendo tambem os volumes persistentes:

```bash
docker compose down -v
```

## Como testar

No diretorio `backend`:

```bash
mvn test
```

Para gerar o pacote sem executar testes:

```bash
mvn -DskipTests package
```

Para executar build completo com testes:

```bash
mvn clean test
```

Os testes cobrem controllers, servicos, seguranca, calculos de pedidos, estoque, disponibilidade, relatorios, autenticacao e upload de imagens.

## Swagger

Com o backend em execucao, acesse:

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

Rotas administrativas usam JWT Bearer.

Fluxo de autenticacao no Swagger:

1. Execute `POST /api/auth/login`.
2. Copie o token retornado.
3. Clique em `Authorize`.
4. Informe:

```text
Bearer {token}
```

## Docker

O projeto possui:

* `backend/Dockerfile`: build multi-stage com Maven e runtime Java 21.
* `backend/.dockerignore`: reduz o contexto de build.
* `docker-compose.yml`: backend + PostgreSQL + volumes + healthchecks.
* `docs/docker.md`: guia detalhado de Docker.

Volumes criados pelo Compose:

* `postgres_data`: dados persistentes do PostgreSQL.
* `produto_uploads`: uploads persistentes de imagens de produtos.

O backend em Docker usa o perfil `prod` e conecta ao PostgreSQL pelo hostname interno `postgres`.

Validar Compose:

```bash
docker compose config
```

## Endpoints principais

Publicos:

```http
GET /api/health
GET /api/produtos
GET /api/produtos/{id}
POST /api/pedidos
GET /api/pedidos/{codigo}
GET /api/pedidos/{codigo}/historico
POST /api/auth/login
GET /uploads/produtos/{arquivo}
```

Administrativos:

```http
GET /api/admin/produtos
GET /api/admin/produtos/{id}
POST /api/admin/produtos
PUT /api/admin/produtos/{id}
POST /api/admin/produtos/{id}/imagem
DELETE /api/admin/produtos/{id}
GET /api/admin/disponibilidade
POST /api/admin/disponibilidade
GET /api/admin/disponibilidade/{id}
PUT /api/admin/disponibilidade/{id}
DELETE /api/admin/disponibilidade/{id}
POST /api/admin/pedidos/vendas-manuais
PATCH /api/admin/pedidos/{id}/status
GET /api/admin/dashboard
GET /api/admin/relatorios/faturamento
GET /api/admin/relatorios/faturamento/diario
GET /api/admin/relatorios/faturamento/semanal
GET /api/admin/relatorios/faturamento/mensal
GET /api/admin/relatorios/faturamento/anual
GET /api/admin/relatorios/produtos-mais-vendidos
GET /api/admin/relatorios/produtos/quantidade-vendida
GET /api/admin/relatorios/ticket-medio
GET /api/admin/relatorios/cidade-mais-pedidos
```

Todas as rotas sob `/api/admin/**` exigem autenticacao de administrador via JWT.

## Upload de imagens

Endpoint:

```http
POST /api/admin/produtos/{id}/imagem
```

Regras:

* Enviar como `multipart/form-data`.
* Campo esperado: `imagem`.
* Formatos aceitos: `image/jpeg`, `image/png`, `image/webp`.
* Tamanho padrao: `5MB`.
* O backend salva o arquivo no disco e grava no banco apenas a URL publica.
* Nao usar Base64.

## Documentacao

Antes de implementar novas funcionalidades, consulte os arquivos em `docs/`.

Documentos principais:

* `docs/visao-geral.md`
* `docs/requisitos.md`
* `docs/regras-negocio.md`
* `docs/api-contract.md`
* `docs/banco-dados.md`
* `docs/openapi-swagger.md`
* `docs/upload-imagens.md`
* `docs/docker.md`
