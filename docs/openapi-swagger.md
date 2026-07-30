# OpenAPI / Swagger

## Objetivo

A API do FinoPaladar e documentada com OpenAPI usando Swagger UI.

Todos os endpoints possuem descricao, parametros, respostas e status HTTP documentados nos controllers.

## Acesso

Swagger UI:

```http
GET /swagger-ui.html
```

Especificacao OpenAPI em JSON:

```http
GET /v3/api-docs
```

## Autenticacao

As rotas administrativas usam JWT Bearer.

No Swagger UI, clique em `Authorize` e informe:

```text
Bearer {token}
```

O token e obtido em:

```http
POST /api/auth/login
```

## Rotas administrativas

Todas as rotas sob:

```http
/api/admin/**
```

exigem autenticacao de administrador.

## Upload de imagens

O endpoint de upload aparece na documentacao como `multipart/form-data`:

```http
POST /api/admin/produtos/{id}/imagem
```

O campo esperado e:

```text
imagem
```

Nao utilizar Base64.
