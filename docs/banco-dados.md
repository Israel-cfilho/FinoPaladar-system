# Estrutura Inicial do Banco de Dados

## Administrador

* id
* nome
* email
* senha
* ativo

## Produto

* id
* nome
* descrição
* preço
* peso médio em gramas
* imagem
* ativo

## DisponibilidadeProduto

* id
* produto
* quantidade disponível

## Pedido

* id
* código
* cliente
* telefone
* status
* forma de pagamento
* tipo de recebimento
* valor dos produtos
* taxa de entrega
* valor total
* observação
* data

## ItemPedido

* id
* pedido
* produto
* nome do produto
* preço unitário
* peso médio
* quantidade
* subtotal

## Endereço

* id
* pedido
* cidade
* tipo de endereço
* condomínio
* quadra
* lote
* bairro
* rua
* número
* complemento
* ponto de referência

## Histórico de Status

* id
* pedido
* status
* data e hora
