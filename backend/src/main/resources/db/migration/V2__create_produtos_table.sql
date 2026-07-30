CREATE TABLE produtos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    descricao TEXT,
    preco NUMERIC(10, 2) NOT NULL,
    peso_medio_gramas INTEGER NOT NULL,
    imagem VARCHAR(500) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_produtos_preco_positivo CHECK (preco > 0),
    CONSTRAINT ck_produtos_peso_medio_positivo CHECK (peso_medio_gramas > 0)
);
