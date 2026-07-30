CREATE TABLE enderecos_entrega (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    tipo_endereco VARCHAR(40) NOT NULL,
    condominio VARCHAR(120),
    quadra VARCHAR(50),
    lote VARCHAR(50),
    bairro VARCHAR(120),
    rua VARCHAR(150),
    numero VARCHAR(30),
    complemento VARCHAR(150),
    ponto_referencia VARCHAR(150),
    CONSTRAINT uk_enderecos_entrega_pedido UNIQUE (pedido_id),
    CONSTRAINT fk_enderecos_entrega_pedido
        FOREIGN KEY (pedido_id)
        REFERENCES pedidos (id)
);
