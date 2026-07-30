package br.com.finopaladar.backend.service;

import br.com.finopaladar.backend.exception.BusinessException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImagemProdutoStorageService {

    private static final Map<String, String> EXTENSOES_PERMITIDAS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final Path diretorio;
    private final String urlPrefix;
    private final long tamanhoMaximoBytes;

    public ImagemProdutoStorageService(
            @Value("${upload.imagens.produtos.diretorio:uploads/produtos}") String diretorio,
            @Value("${upload.imagens.produtos.url-prefix:/uploads/produtos}") String urlPrefix,
            @Value("${upload.imagens.max-size-bytes:5242880}") long tamanhoMaximoBytes
    ) {
        this.diretorio = Path.of(diretorio).toAbsolutePath().normalize();
        this.urlPrefix = normalizarUrlPrefix(urlPrefix);
        this.tamanhoMaximoBytes = tamanhoMaximoBytes;
    }

    public String salvar(MultipartFile imagem) {
        validarImagem(imagem);

        String extensao = EXTENSOES_PERMITIDAS.get(imagem.getContentType());
        String nomeArquivo = UUID.randomUUID() + "." + extensao;
        Path destino = diretorio.resolve(nomeArquivo).normalize();
        if (!destino.startsWith(diretorio)) {
            throw new BusinessException("Nome de arquivo invalido");
        }

        try {
            Files.createDirectories(diretorio);
            try (InputStream inputStream = imagem.getInputStream()) {
                Files.copy(inputStream, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel salvar a imagem", exception);
        }

        return urlPrefix + "/" + nomeArquivo;
    }

    private void validarImagem(MultipartFile imagem) {
        if (imagem == null || imagem.isEmpty()) {
            throw new BusinessException("Imagem deve ser informada");
        }
        if (imagem.getSize() > tamanhoMaximoBytes) {
            throw new BusinessException("Imagem excede o tamanho maximo permitido");
        }
        if (!EXTENSOES_PERMITIDAS.containsKey(imagem.getContentType())) {
            throw new BusinessException("Formato de imagem invalido");
        }
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
