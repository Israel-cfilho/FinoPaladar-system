package br.com.finopaladar.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.finopaladar.backend.dto.DisponibilidadeProdutoRequest;
import br.com.finopaladar.backend.dto.DisponibilidadeProdutoResponse;
import br.com.finopaladar.backend.entity.DisponibilidadeProduto;
import br.com.finopaladar.backend.entity.Produto;
import br.com.finopaladar.backend.exception.BusinessException;
import br.com.finopaladar.backend.exception.ResourceNotFoundException;
import br.com.finopaladar.backend.mapper.DisponibilidadeProdutoMapper;
import br.com.finopaladar.backend.repository.DisponibilidadeProdutoRepository;
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

@ExtendWith(MockitoExtension.class)
class DisponibilidadeProdutoServiceTest {

    @Mock
    private DisponibilidadeProdutoRepository disponibilidadeProdutoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    private DisponibilidadeProdutoService disponibilidadeProdutoService;

    @BeforeEach
    void setUp() {
        disponibilidadeProdutoService = new DisponibilidadeProdutoService(
                disponibilidadeProdutoRepository,
                produtoRepository,
                new DisponibilidadeProdutoMapper()
        );
    }

    @Test
    void deveCadastrarDisponibilidadeParaProdutoExistente() {
        Produto produto = produto(1L);
        DisponibilidadeProdutoRequest request = request(1L, 10);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(disponibilidadeProdutoRepository.existsByProdutoId(1L)).thenReturn(false);
        when(disponibilidadeProdutoRepository.save(any(DisponibilidadeProduto.class))).thenAnswer(invocation -> {
            DisponibilidadeProduto disponibilidadeProduto = invocation.getArgument(0);
            disponibilidadeProduto.setId(1L);
            return disponibilidadeProduto;
        });

        DisponibilidadeProdutoResponse response = disponibilidadeProdutoService.cadastrar(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.produtoId()).isEqualTo(1L);
        assertThat(response.produtoNome()).isEqualTo("Bolo de Rolo");
        assertThat(response.quantidadeDisponivel()).isEqualTo(10);
        assertThat(response.dataInicial()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(response.dataFinal()).isEqualTo(LocalDate.of(2026, 8, 2));
        verify(disponibilidadeProdutoRepository).save(any(DisponibilidadeProduto.class));
    }

    @Test
    void naoDeveCadastrarDisponibilidadeParaProdutoInexistente() {
        DisponibilidadeProdutoRequest request = request(99L, 10);

        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> disponibilidadeProdutoService.cadastrar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Produto nao encontrado");

        verify(disponibilidadeProdutoRepository, never()).save(any());
    }

    @Test
    void naoDeveCadastrarQuantidadeNegativa() {
        DisponibilidadeProdutoRequest request = request(1L, -1);

        assertThatThrownBy(() -> disponibilidadeProdutoService.cadastrar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Quantidade disponivel nao pode ser negativa");

        verifyNoBuscaProdutoNemSalvar();
    }

    @Test
    void naoDeveCadastrarPeriodoInvalido() {
        DisponibilidadeProdutoRequest request = new DisponibilidadeProdutoRequest(
                1L,
                10,
                LocalDate.of(2026, 8, 2),
                LocalDate.of(2026, 8, 2)
        );

        assertThatThrownBy(() -> disponibilidadeProdutoService.cadastrar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Data final deve ser maior que data inicial");

        verifyNoBuscaProdutoNemSalvar();
    }

    @Test
    void naoDeveCadastrarDisponibilidadeDuplicadaParaMesmoProduto() {
        Produto produto = produto(1L);
        DisponibilidadeProdutoRequest request = request(1L, 10);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(disponibilidadeProdutoRepository.existsByProdutoId(1L)).thenReturn(true);

        assertThatThrownBy(() -> disponibilidadeProdutoService.cadastrar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Produto ja possui disponibilidade cadastrada");

        verify(disponibilidadeProdutoRepository, never()).save(any());
    }

    @Test
    void deveConsultarDisponibilidades() {
        DisponibilidadeProduto disponibilidadeProduto = disponibilidadeProduto(1L, produto(1L), 8);

        when(disponibilidadeProdutoRepository.findAllByOrderByDataInicialAsc())
                .thenReturn(List.of(disponibilidadeProduto));

        List<DisponibilidadeProdutoResponse> responses = disponibilidadeProdutoService.consultar();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(1L);
        assertThat(responses.getFirst().quantidadeDisponivel()).isEqualTo(8);
    }

    @Test
    void deveEditarDisponibilidade() {
        Produto produto = produto(1L);
        DisponibilidadeProduto disponibilidadeProduto = disponibilidadeProduto(1L, produto, 5);
        DisponibilidadeProdutoRequest request = request(1L, 12);

        when(disponibilidadeProdutoRepository.findById(1L)).thenReturn(Optional.of(disponibilidadeProduto));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(disponibilidadeProdutoRepository.existsByProdutoIdAndIdNot(1L, 1L)).thenReturn(false);
        when(disponibilidadeProdutoRepository.save(disponibilidadeProduto)).thenReturn(disponibilidadeProduto);

        DisponibilidadeProdutoResponse response = disponibilidadeProdutoService.editar(1L, request);

        assertThat(response.quantidadeDisponivel()).isEqualTo(12);
        assertThat(disponibilidadeProduto.getQuantidadeDisponivel()).isEqualTo(12);
    }

    @Test
    void deveExcluirDisponibilidade() {
        DisponibilidadeProduto disponibilidadeProduto = disponibilidadeProduto(1L, produto(1L), 5);

        when(disponibilidadeProdutoRepository.findById(1L)).thenReturn(Optional.of(disponibilidadeProduto));

        disponibilidadeProdutoService.excluir(1L);

        verify(disponibilidadeProdutoRepository).delete(disponibilidadeProduto);
    }

    private DisponibilidadeProdutoRequest request(Long produtoId, Integer quantidadeDisponivel) {
        return new DisponibilidadeProdutoRequest(
                produtoId,
                quantidadeDisponivel,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 2)
        );
    }

    private Produto produto(Long id) {
        Produto produto = new Produto();
        produto.setId(id);
        produto.setNome("Bolo de Rolo");
        produto.setDescricao("Tradicional");
        produto.setPreco(new BigDecimal("45.90"));
        produto.setPesoMedioGramas(500);
        produto.setImagem("https://example.com/bolo.jpg");
        produto.setAtivo(true);
        return produto;
    }

    private DisponibilidadeProduto disponibilidadeProduto(Long id, Produto produto, Integer quantidadeDisponivel) {
        DisponibilidadeProduto disponibilidadeProduto = new DisponibilidadeProduto();
        disponibilidadeProduto.setId(id);
        disponibilidadeProduto.setProduto(produto);
        disponibilidadeProduto.setQuantidadeDisponivel(quantidadeDisponivel);
        disponibilidadeProduto.setDataInicial(LocalDate.of(2026, 8, 1));
        disponibilidadeProduto.setDataFinal(LocalDate.of(2026, 8, 2));
        return disponibilidadeProduto;
    }

    private void verifyNoBuscaProdutoNemSalvar() {
        verify(produtoRepository, never()).findById(any());
        verify(disponibilidadeProdutoRepository, never()).save(any());
    }
}
