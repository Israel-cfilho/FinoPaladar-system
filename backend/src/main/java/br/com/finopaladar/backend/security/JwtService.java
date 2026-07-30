package br.com.finopaladar.backend.security;

import br.com.finopaladar.backend.entity.Administrador;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String ALGORITMO = "HmacSHA256";
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final String secret;
    private final long expirationMinutes;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-minutes:120}") long expirationMinutes
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("JWT secret deve ser configurado");
        }
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.expirationMinutes = expirationMinutes;
    }

    public JwtToken gerarToken(Administrador administrador) {
        Instant emitidoEm = Instant.now();
        Instant expiraEm = emitidoEm.plusSeconds(expirationMinutes * 60);

        Map<String, Object> header = Map.of(
                "alg", "HS256",
                "typ", "JWT"
        );
        Map<String, Object> payload = Map.of(
                "sub", administrador.getEmail(),
                "administradorId", administrador.getId(),
                "nome", administrador.getNome(),
                "role", "ADMIN",
                "iat", emitidoEm.getEpochSecond(),
                "exp", expiraEm.getEpochSecond()
        );

        String headerBase64 = toBase64Json(header);
        String payloadBase64 = toBase64Json(payload);
        String conteudoAssinado = headerBase64 + "." + payloadBase64;
        String assinatura = assinar(conteudoAssinado);

        return new JwtToken(conteudoAssinado + "." + assinatura, expiraEm);
    }

    public Optional<JwtClaims> validarToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        try {
            String[] partes = token.split("\\.");
            if (partes.length != 3) {
                return Optional.empty();
            }

            String conteudoAssinado = partes[0] + "." + partes[1];
            String assinaturaEsperada = assinar(conteudoAssinado);
            if (!assinaturaConfere(assinaturaEsperada, partes[2])) {
                return Optional.empty();
            }

            Map<String, Object> claims = fromBase64Json(partes[1]);
            String subject = (String) claims.get("sub");
            Number exp = (Number) claims.get("exp");
            if (subject == null || subject.isBlank() || exp == null) {
                return Optional.empty();
            }

            Instant expiraEm = Instant.ofEpochSecond(exp.longValue());
            if (!expiraEm.isAfter(Instant.now())) {
                return Optional.empty();
            }

            return Optional.of(new JwtClaims(subject, expiraEm));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private String toBase64Json(Map<String, Object> valor) {
        try {
            return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(valor));
        } catch (Exception exception) {
            throw new IllegalStateException("Nao foi possivel gerar JWT", exception);
        }
    }

    private Map<String, Object> fromBase64Json(String valor) {
        try {
            byte[] json = BASE64_URL_DECODER.decode(valor);
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception exception) {
            throw new IllegalArgumentException("JWT invalido", exception);
        }
    }

    private String assinar(String conteudo) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITMO);
            mac.init(secretKey);
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(conteudo.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Nao foi possivel assinar JWT", exception);
        }
    }

    private boolean assinaturaConfere(String assinaturaEsperada, String assinaturaRecebida) {
        return MessageDigest.isEqual(
                assinaturaEsperada.getBytes(StandardCharsets.US_ASCII),
                assinaturaRecebida.getBytes(StandardCharsets.US_ASCII)
        );
    }
}
