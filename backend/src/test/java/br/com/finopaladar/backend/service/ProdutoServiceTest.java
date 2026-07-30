package br.com.finopaladar.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.finopaladar.backend.dto.ProdutoRequest;
import br.com.finopaladar.backend.dto.ProdutoPublicoResponse;
import br.com.finopaladar.backend.dto.ProdutoResponse;
import br.com.finopaladar.backend.entity.DisponibilidadeProduto;
import br.com.finopaladar.backend.entity.Produto;
import br.com.finopaladar.backend.exception.ResourceNotFoundException;
import br.com.finopaladar.backend.mapper.ProdutoMapper;
import br.com.finopaladar.backend.repository.ProdutoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ImagemProdutoStorageService imagemProdutoStorageService;

    private ProdutoService produtoService;

    @BeforeEach
    void setUp() {
        produtoService = new ProdutoService(produtoRepository, new ProdutoMapper(), imagemProdutoStorageService);
    }

    @Test
    void deveCriarProduto() {
        ProdutoRequest request = produtoRequest();

        when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> {
            Produto produto = invocation.getArgument(0);
            produto.setId(1L);
            return produto;
        });

        ProdutoResponse response = produtoService.criar(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Bolo de Rolo");
        assertThat(response.preco()).isEqualByComparingTo("45.90");
        assertThat(response.ativo()).isTrue();
        verify(produtoRepository).save(any(Produto.class));
    }

    @Test
    void deveListarApenasProdutosDisponiveisParaClientes() {
        Produto produtoDisponivel = produto(1L, "Bolo Tradicional", true);
        produtoDisponivel.setDisponibilidadeProduto(disponibilidadeProduto(produtoDisponivel, 7));

        when(produtoRepository.findProdutosDisponiveisParaCatalogo(any(LocalDate.class)))
                .thenReturn(List.of(produtoDisponivel));

        List<ProdutoPublicoResponse> responses = produtoService.listarProdutosPublicos();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(1L);
        assertThat(responses.getFirst().nome()).isEqualTo("Bolo Tradicional");
        assertThat(responses.getFirst().quantidadeDisponivel()).isEqualTo(7);
        verify(produtoRepository).findProdutosDisponiveisParaCatalogo(any(LocalDate.class));
        verify(produtoRepository, never()).findAllByOrderByNomeAsc();
    }

    @Test
    void deveBuscarProdutoDisponivelParaCliente() {
        Produto produtoDisponivel = produto(1L, "Bolo Tradicional", true);
        produtoDisponivel.setDisponibilidadeProduto(disponibilidadeProduto(produtoDisponivel, 7));

        when(produtoRepository.findProdutoDisponivelParaCatalogoPorId(any(Long.class), any(LocalDate.class)))
                .thenReturn(Optional.of(produtoDisponivel));

        ProdutoPublicoResponse response = produtoService.buscarProdutoPublicoPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.quantidadeDisponivel()).isEqualTo(7);
    }

    @Test
    void deveAtualizarProdutoExistente() {
        Produto produto = produto(1L, "Nome antigo", true);
        ProdutoRequest request = new ProdutoRequest(
                "Nome novo",
                "Descricao nova",
                new BigDecimal("55.00"),
                650,
                "https://example.com/novo.jpg",
                false
        );

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(produto)).thenReturn(produto);

        ProdutoResponse response = produtoService.atualizar(1L, request);

        assertThat(response.nome()).isEqualTo("Nome novo");
        assertThat(response.descricao()).isEqualTo("Descricao nova");
        assertThat(response.preco()).isEqualByComparingTo("55.00");
        assertThat(response.pesoMedioGramas()).isEqualTo(650);
        assertThat(response.imagem()).isEqualTo("https://example.com/novo.jpg");
        assertThat(response.ativo()).isFalse();
    }

    @Test
    void deveAtualizarImagemDoProdutoSalvandoUrlNoBanco() {
        Produto produto = produto(1L, "Bolo de Rolo", true);
        MockMultipartFile imagem = new MockMultipartFile(
                "imagem",
                "bolo.png",
                "image/png",
                "conteudo".getBytes()
        );
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(imagemProdutoStorageService.salvar(imagem)).thenReturn("/uploads/produtos/bolo.png");
        when(produtoRepository.save(produto)).thenReturn(produto);

        ProdutoResponse response = produtoService.atualizarImagem(1L, imagem);

        assertThat(produto.getImagem()).isEqualTo("/uploads/produtos/bolo.png");
        assertThat(response.imagem()).isEqualTo("/uploads/produtos/bolo.png");
        verify(imagemProdutoStorageService).salvar(imagem);
        verify(produtoRepository).save(produto);
    }

    @Test
    void deveInativarProdutoAoExcluir() {
        Produto produto = produto(1L, "Bolo de Rolo", true);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(produto)).thenReturn(produto);

        produtoService.excluir(1L);

        assertThat(produto.getAtivo()).isFalse();
        verify(produtoRepository).save(produto);
    }

    @Test
    void deveLancarErroAoBuscarProdutoInexistente() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Produto nao encontrado");
    }

    private ProdutoRequest produtoRequest() {
        return new ProdutoRequest(
                "Bolo de Rolo",
                "Tradicional",
                new BigDecimal("45.90"),
                500,
                "https://example.com/bolo.jpg",
                true
        );
    }

    private Produto produto(Long id, String nome, Boolean ativo) {
        Produto produto = new Produto();
        produto.setId(id);
        produto.setNome(nome);
        produto.setDescricao("Tradicional");
        produto.setPreco(new BigDecimal("45.90"));
        produto.setPesoMedioGramas(500);
        produto.setImagem("https://example.com/bolo.jpg");
        produto.setAtivo(ativo);
        return produto;
    }

    private DisponibilidadeProduto disponibilidadeProduto(Produto produto, Integer quantidadeDisponivel) {
        DisponibilidadeProduto disponibilidadeProduto = new DisponibilidadeProduto();
        disponibilidadeProduto.setId(1L);
        disponibilidadeProduto.setProduto(produto);
        disponibilidadeProduto.setQuantidadeDisponivel(quantidadeDisponivel);
        return disponibilidadeProduto;
    }
}
