ALTER TABLE pedidos
    ADD COLUMN canal_venda VARCHAR(20);

ALTER TABLE pedidos
    ADD CONSTRAINT ck_pedidos_canal_venda
        CHECK (
            canal_venda IS NULL
            OR canal_venda IN ('WHATSAPP', 'TELEFONE', 'PRESENCIAL')
        );
