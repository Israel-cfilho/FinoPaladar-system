CREATE TABLE pedidos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL,
    cliente VARCHAR(150) NOT NULL,
    telefone VARCHAR(30) NOT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'AGUARDANDO_CONFIRMACAO',
    forma_pagamento VARCHAR(60) NOT NULL,
    tipo_recebimento VARCHAR(20) NOT NULL,
    valor_produtos NUMERIC(10, 2) NOT NULL,
    taxa_entrega NUMERIC(10, 2) NOT NULL DEFAULT 0,
    valor_total NUMERIC(10, 2) NOT NULL,
    observacao TEXT,
    data DATE NOT NULL,
    CONSTRAINT uk_pedidos_codigo UNIQUE (codigo),
    CONSTRAINT ck_pedidos_status
        CHECK (status IN (
            'AGUARDANDO_CONFIRMACAO',
            'ACEITO',
            'EM_PREPARACAO',
            'PRONTO_PARA_RETIRADA',
            'SAIU_PARA_ENTREGA',
            'ENTREGUE',
            'CANCELADO'
        )),
    CONSTRAINT ck_pedidos_tipo_recebimento
        CHECK (tipo_recebimento IN ('RETIRADA', 'ENTREGA')),
    CONSTRAINT ck_pedidos_valor_produtos_nao_negativo CHECK (valor_produtos >= 0),
    CONSTRAINT ck_pedidos_taxa_entrega_nao_negativa CHECK (taxa_entrega >= 0),
    CONSTRAINT ck_pedidos_valor_total_nao_negativo CHECK (valor_total >= 0)
);

CREATE INDEX idx_pedidos_status ON pedidos (status);
CREATE INDEX idx_pedidos_data ON pedidos (data);
