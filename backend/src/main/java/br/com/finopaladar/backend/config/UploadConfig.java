package br.com.finopaladar.backend.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UploadConfig implements WebMvcConfigurer {

    private final Path diretorioProdutos;
    private final String urlPrefixProdutos;

    public UploadConfig(
            @Value("${upload.imagens.produtos.diretorio:uploads/produtos}") String diretorioProdutos,
            @Value("${upload.imagens.produtos.url-prefix:/uploads/produtos}") String urlPrefixProdutos
    ) {
        this.diretorioProdutos = Path.of(diretorioProdutos).toAbsolutePath().normalize();
        this.urlPrefixProdutos = normalizarUrlPrefix(urlPrefixProdutos);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(urlPrefixProdutos + "/**")
                .addResourceLocations(diretorioProdutos.toUri().toString());
    }

    private String normalizarUrlPrefix(String valor) {
        if (valor == null || valor.isBlank()) {
            return "/uploads/produtos";
        }

        String prefixo = valor.trim();
        if (!prefixo.startsWith("/")) {
            prefixo = "/" + prefixo;
        }
        if (prefixo.endsWith("/")) {
            return prefixo.substring(0, prefixo.length() - 1);
        }

        return prefixo;
    }
}
