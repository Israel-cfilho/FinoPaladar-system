ALTER TABLE disponibilidades_produto
    ADD COLUMN data_inicial DATE,
    ADD COLUMN data_final DATE;

UPDATE disponibilidades_produto
SET data_inicial = CURRENT_DATE,
    data_final = CURRENT_DATE + 1
WHERE data_inicial IS NULL
   OR data_final IS NULL;

ALTER TABLE disponibilidades_produto
    ALTER COLUMN data_inicial SET NOT NULL,
    ALTER COLUMN data_final SET NOT NULL,
    ADD CONSTRAINT ck_disponibilidades_produto_periodo
        CHECK (data_final > data_inicial);
