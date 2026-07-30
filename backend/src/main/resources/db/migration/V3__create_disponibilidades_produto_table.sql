CREATE TABLE disponibilidades_produto (
    id BIGSERIAL PRIMARY KEY,
    produto_id BIGINT NOT NULL,
    quantidade_disponivel INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uk_disponibilidades_produto_produto UNIQUE (produto_id),
    CONSTRAINT fk_disponibilidades_produto_produto
        FOREIGN KEY (produto_id)
        REFERENCES produtos (id),
    CONSTRAINT ck_disponibilidades_produto_quantidade_nao_negativa
        CHECK (quantidade_disponivel >= 0)
);
