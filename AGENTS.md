# AGENTS.md

# Sistema de Vendas - Bolos de Rolo

## Objetivo do projeto

Desenvolver um sistema web completo para gerenciamento de vendas de bolos de rolo.

O sistema será composto por:

* Backend em Java com Spring Boot;
* Frontend em React + TypeScript;
* Banco de dados PostgreSQL;
* Integração com WhatsApp;
* Painel administrativo;
* Área pública para clientes.

Antes de implementar qualquer funcionalidade, consulte toda a documentação presente na pasta `/docs`.

---

# Tecnologias obrigatórias

## Backend

* Java 21
* Spring Boot
* Spring Data JPA
* Spring Validation
* Spring Security
* JWT
* PostgreSQL
* Flyway
* Maven

## Frontend

* React
* TypeScript
* Tailwind CSS
* React Router
* Axios

---

# Arquitetura

O backend deverá seguir arquitetura em camadas.

```text
Controller

↓

Service

↓

Repository

↓

Banco de Dados
```

Nunca colocar regra de negócio dentro dos Controllers.

Toda regra deverá ficar na camada Service.

---

# Organização das pastas

Backend:

```text
controller
service
repository
entity
dto
mapper
config
exception
security
validation
util
```

Frontend:

```text
components
pages
layouts
hooks
services
types
utils
assets
```

---

# Banco de dados

Utilizar PostgreSQL.

Criar migrations utilizando Flyway.

Nunca depender de criação automática de tabelas em produção.

---

# API

Toda comunicação deverá ocorrer através de APIs REST.

Os Controllers nunca deverão retornar entidades JPA diretamente.

Sempre utilizar DTOs.

---

# DTOs

Separar DTOs de entrada e saída.

Exemplo:

```text
ProdutoRequest
ProdutoResponse

PedidoRequest
PedidoResponse
```

Nunca reutilizar entidades como objetos de entrada.

---

# Validações

Toda entrada deverá ser validada utilizando Bean Validation.

Exemplos:

* campos obrigatórios;
* telefone válido;
* preço positivo;
* quantidade maior que zero;
* peso médio maior que zero.

---

# Valores monetários

Nunca utilizar float ou double.

Sempre utilizar:

```java
BigDecimal
```

---

# Datas

Utilizar:

```java
LocalDate
LocalDateTime
Instant
```

Evitar utilizar java.util.Date.

---

# Estoque

O estoque nunca poderá ficar negativo.

Toda movimentação deverá acontecer dentro de transações.

Ao cancelar um pedido, a quantidade deverá retornar automaticamente ao estoque.

---

# Produtos

Todo produto deverá possuir:

* nome;
* descrição;
* preço;
* peso médio em gramas;
* imagem;
* ativo;
* quantidade disponível.

Produtos inativos não poderão aparecer para clientes.

---

# Pedidos

O backend será responsável por:

* validar produtos;
* validar disponibilidade;
* calcular subtotais;
* calcular total;
* calcular taxa de entrega;
* copiar nome, preço e peso do produto para ItemPedido.

Nunca confiar em valores enviados pelo frontend.

O frontend enviará apenas:

* produto;
* quantidade.

Todo o restante deverá ser calculado pelo backend.

---

# Faturamento

Entram no faturamento apenas pedidos com status:

```text
ENTREGUE
```

Pedidos cancelados nunca entram no faturamento.

---

# Status dos pedidos

Fluxo permitido:

```text
AGUARDANDO_CONFIRMACAO

↓

ACEITO

↓

EM_PREPARACAO

↓

PRONTO_PARA_RETIRADA

ou

SAIU_PARA_ENTREGA

↓

ENTREGUE
```

Também será permitido:

```text
AGUARDANDO_CONFIRMACAO

↓

CANCELADO
```

e

```text
ACEITO

↓

CANCELADO
```

Não permitir alterações inválidas de status.

---

# Segurança

Apenas administradores autenticados poderão acessar:

* produtos administrativos;
* disponibilidade;
* pedidos;
* dashboard;
* relatórios.

Clientes não precisarão criar conta.

---

# Tratamento de erros

Criar um tratamento global utilizando:

```text
@RestControllerAdvice
```

Todos os erros deverão retornar mensagens padronizadas.

---

# Código

Priorizar código simples e legível.

Evitar duplicação.

Criar métodos pequenos.

Utilizar nomes descritivos.

Evitar comentários desnecessários.

O código deve ser autoexplicativo.

---

# Testes

Toda regra de negócio importante deverá possuir testes.

Exemplos:

* cálculo do total;
* redução do estoque;
* devolução do estoque;
* criação de pedidos;
* cancelamento;
* validação de disponibilidade.

---

# Performance

Evitar consultas N+1.

Paginar listagens administrativas.

Buscar apenas os dados necessários.

---

# Frontend

Toda comunicação deverá acontecer através da API.

Nunca calcular valores financeiros no frontend.

O frontend apenas exibirá os valores enviados pelo backend.

---

# WhatsApp

Após salvar o pedido:

1. registrar o pedido no banco;
2. gerar a mensagem;
3. gerar o link do WhatsApp;
4. retornar essas informações para o frontend.

O backend nunca deverá abrir o WhatsApp.

---

# Documentação

Sempre consultar os arquivos presentes na pasta `/docs` antes de implementar novas funcionalidades.

Caso exista conflito entre o código e a documentação, considerar a documentação como fonte oficial.

---

# Regras para o Codex

Antes de iniciar qualquer tarefa:

1. Leia o AGENTS.md.
2. Leia todos os arquivos da pasta `/docs`.
3. Faça apenas o que foi solicitado.
4. Não implemente funcionalidades extras.
5. Não altere arquivos que não fazem parte da tarefa.
6. Explique resumidamente as alterações realizadas.
7. Execute os testes antes de finalizar.
8. Caso encontre ambiguidades, sinalize-as em vez de assumir comportamentos.

---

# Objetivo final

Construir um sistema de qualidade profissional, organizado, seguro, escalável e de fácil manutenção, priorizando código limpo, boas práticas, testes e documentação.
