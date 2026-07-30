# Upload de Imagens

## Objetivo

Permitir que o administrador envie imagens de produtos usando arquivo real via `multipart/form-data`.

O backend salva o arquivo no disco e grava no banco apenas a URL publica da imagem no campo `imagem` do produto.

Nao utilizar Base64.

## Endpoint

```http
POST /api/admin/produtos/{id}/imagem
```

Rota administrativa. Requer autenticacao de administrador via JWT.

## Request

Content-Type:

```text
multipart/form-data
```

Campo:

```text
imagem
```

Formatos aceitos:

```text
image/jpeg
image/png
image/webp
```

Tamanho padrao:

```text
5MB
```

## Response

Retorna o produto atualizado.

Exemplo:

```json
{
  "id": 1,
  "nome": "Bolo de Rolo",
  "descricao": "Tradicional",
  "preco": 45.90,
  "pesoMedioGramas": 500,
  "imagem": "/uploads/produtos/arquivo.png",
  "ativo": true
}
```

## Configuracao

Variaveis de ambiente:

```text
UPLOAD_IMAGE_DIR=uploads/produtos
UPLOAD_IMAGE_URL_PREFIX=/uploads/produtos
UPLOAD_IMAGE_MAX_SIZE_BYTES=5242880
UPLOAD_IMAGE_MAX_FILE_SIZE=5MB
UPLOAD_IMAGE_MAX_REQUEST_SIZE=5MB
```

## Acesso publico

As imagens salvas ficam disponiveis publicamente por:

```http
GET /uploads/produtos/{arquivo}
```
