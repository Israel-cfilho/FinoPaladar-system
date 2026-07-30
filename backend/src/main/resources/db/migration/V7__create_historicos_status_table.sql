CREATE TABLE historicos_status (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    status VARCHAR(40) NOT NULL,
    data_hora TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_historicos_status_pedido
        FOREIGN KEY (pedido_id)
        REFERENCES pedidos (id),
    CONSTRAINT ck_historicos_status_status
        CHECK (status IN (
            'AGUARDANDO_CONFIRMACAO',
            'ACEITO',
            'EM_PREPARACAO',
            'PRONTO_PARA_RETIRADA',
            'SAIU_PARA_ENTREGA',
            'ENTREGUE',
            'CANCELADO'
        ))
);

CREATE INDEX idx_historicos_status_pedido ON historicos_status (pedido_id);
CREATE INDEX idx_historicos_status_data_hora ON historicos_status (data_hora);
