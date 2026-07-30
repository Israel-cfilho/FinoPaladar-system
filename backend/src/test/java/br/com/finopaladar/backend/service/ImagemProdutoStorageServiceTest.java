package br.com.finopaladar.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.finopaladar.backend.exception.BusinessException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class ImagemProdutoStorageServiceTest {

    @TempDir
    private Path tempDir;

    @Test
    void deveSalvarImagemRetornandoUrlPublica() throws Exception {
        ImagemProdutoStorageService service = new ImagemProdutoStorageService(
                tempDir.toString(),
                "/uploads/produtos",
                1024
        );
        MockMultipartFile imagem = new MockMultipartFile(
                "imagem",
                "bolo.png",
                "image/png",
                "conteudo".getBytes()
        );

        String url = service.salvar(imagem);

        assertThat(url).startsWith("/uploads/produtos/");
        assertThat(url).endsWith(".png");
        try (var arquivos = Files.list(tempDir)) {
            List<Path> arquivosSalvos = arquivos.toList();
            assertThat(arquivosSalvos).hasSize(1);
            assertThat(Files.readString(arquivosSalvos.getFirst())).isEqualTo("conteudo");
        }
    }

    @Test
    void deveRejeitarArquivoQueNaoSejaImagem() {
        ImagemProdutoStorageService service = new ImagemProdutoStorageService(
                tempDir.toString(),
                "/uploads/produtos",
                1024
        );
        MockMultipartFile arquivo = new MockMultipartFile(
                "imagem",
                "arquivo.txt",
                "text/plain",
                "conteudo".getBytes()
        );

        assertThatThrownBy(() -> service.salvar(arquivo))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Formato de imagem invalido");
    }

    @Test
    void deveRejeitarImagemVazia() {
        ImagemProdutoStorageService service = new ImagemProdutoStorageService(
                tempDir.toString(),
                "/uploads/produtos",
                1024
        );
        MockMultipartFile imagem = new MockMultipartFile(
                "imagem",
                "bolo.png",
                "image/png",
                new byte[0]
        );

        assertThatThrownBy(() -> service.salvar(imagem))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Imagem deve ser informada");
    }
}
