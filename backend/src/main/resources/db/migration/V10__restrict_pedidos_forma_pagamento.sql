ALTER TABLE pedidos
    ALTER COLUMN forma_pagamento TYPE VARCHAR(20);

ALTER TABLE pedidos
    ADD CONSTRAINT ck_pedidos_forma_pagamento
        CHECK (forma_pagamento IN ('PIX', 'DINHEIRO'));
