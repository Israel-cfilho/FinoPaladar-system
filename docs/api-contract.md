# Endpoints Planejados

## Produtos

GET /api/produtos

GET /api/produtos/{id}

## Pedidos

POST /api/pedidos

GET /api/pedidos/{codigo}

## Administração

POST /api/auth/login

GET /api/admin/pedidos

PATCH /api/admin/pedidos/{id}/status

POST /api/admin/produtos

PUT /api/admin/produtos/{id}

POST /api/admin/produtos/{id}/imagem

DELETE /api/admin/produtos/{id}

POST /api/admin/disponibilidade

GET /api/admin/relatorios/faturamento

GET /api/admin/relatorios/produtos-mais-vendidos
