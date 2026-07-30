CREATE TABLE itens_pedido (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    nome_produto VARCHAR(150) NOT NULL,
    preco_unitario NUMERIC(10, 2) NOT NULL,
    peso_medio_gramas INTEGER NOT NULL,
    quantidade INTEGER NOT NULL,
    subtotal NUMERIC(10, 2) NOT NULL,
    CONSTRAINT fk_itens_pedido_pedido
        FOREIGN KEY (pedido_id)
        REFERENCES pedidos (id),
    CONSTRAINT fk_itens_pedido_produto
        FOREIGN KEY (produto_id)
        REFERENCES produtos (id),
    CONSTRAINT ck_itens_pedido_preco_unitario_positivo CHECK (preco_unitario > 0),
    CONSTRAINT ck_itens_pedido_peso_medio_positivo CHECK (peso_medio_gramas > 0),
    CONSTRAINT ck_itens_pedido_quantidade_positiva CHECK (quantidade > 0),
    CONSTRAINT ck_itens_pedido_subtotal_nao_negativo CHECK (subtotal >= 0)
);

CREATE INDEX idx_itens_pedido_pedido ON itens_pedido (pedido_id);
CREATE INDEX idx_itens_pedido_produto ON itens_pedido (produto_id);
