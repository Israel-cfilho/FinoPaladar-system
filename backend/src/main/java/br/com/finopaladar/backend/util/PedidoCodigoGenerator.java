package br.com.finopaladar.backend.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PedidoCodigoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyyMMddHHmmss")
            .withZone(ZoneOffset.UTC);

    public String gerar() {
        String timestamp = FORMATTER.format(Instant.now());
        String sufixo = UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);

        return "PED-" + timestamp + "-" + sufixo;
    }
}
