ALTER TABLE enderecos_entrega
    ALTER COLUMN cidade TYPE VARCHAR(20);

ALTER TABLE enderecos_entrega
    ADD CONSTRAINT ck_enderecos_entrega_cidade
        CHECK (cidade IN ('BANANEIRAS', 'SOLANEA'));
