package br.com.finopaladar.backend.service;

import br.com.finopaladar.backend.dto.DisponibilidadeProdutoRequest;
import br.com.finopaladar.backend.dto.DisponibilidadeProdutoResponse;
import br.com.finopaladar.backend.entity.DisponibilidadeProduto;
import br.com.finopaladar.backend.entity.Produto;
import br.com.finopaladar.backend.exception.BusinessException;
import br.com.finopaladar.backend.exception.ResourceNotFoundException;
import br.com.finopaladar.backend.mapper.DisponibilidadeProdutoMapper;
import br.com.finopaladar.backend.repository.DisponibilidadeProdutoRepository;
import br.com.finopaladar.backend.repository.ProdutoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DisponibilidadeProdutoService {

    private final DisponibilidadeProdutoRepository disponibilidadeProdutoRepository;
    private final ProdutoRepository produtoRepository;
    private final DisponibilidadeProdutoMapper disponibilidadeProdutoMapper;

    public DisponibilidadeProdutoService(
            DisponibilidadeProdutoRepository disponibilidadeProdutoRepository,
            ProdutoRepository produtoRepository,
            DisponibilidadeProdutoMapper disponibilidadeProdutoMapper
    ) {
        this.disponibilidadeProdutoRepository = disponibilidadeProdutoRepository;
        this.produtoRepository = produtoRepository;
        this.disponibilidadeProdutoMapper = disponibilidadeProdutoMapper;
    }

    @Transactional
    public DisponibilidadeProdutoResponse cadastrar(DisponibilidadeProdutoRequest request) {
        validarQuantidade(request);
        validarPeriodo(request);
        Produto produto = buscarProduto(request.produtoId());
        validarProdutoSemDisponibilidade(produto.getId());

        DisponibilidadeProduto disponibilidadeProduto = disponibilidadeProdutoMapper.toEntity(request, produto);
        DisponibilidadeProduto disponibilidadeSalva = disponibilidadeProdutoRepository.save(disponibilidadeProduto);
        return disponibilidadeProdutoMapper.toResponse(disponibilidadeSalva);
    }

    @Transactional(readOnly = true)
    public List<DisponibilidadeProdutoResponse> consultar() {
        return disponibilidadeProdutoRepository.findAllByOrderByDataInicialAsc()
                .stream()
                .map(disponibilidadeProdutoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DisponibilidadeProdutoResponse consultarPorId(Long id) {
        DisponibilidadeProduto disponibilidadeProduto = buscarDisponibilidade(id);
        return disponibilidadeProdutoMapper.toResponse(disponibilidadeProduto);
    }

    @Transactional
    public DisponibilidadeProdutoResponse editar(Long id, DisponibilidadeProdutoRequest request) {
        validarQuantidade(request);
        validarPeriodo(request);
        DisponibilidadeProduto disponibilidadeProduto = buscarDisponibilidade(id);
        Produto produto = buscarProduto(request.produtoId());
        validarProdutoSemOutraDisponibilidade(produto.getId(), id);

        disponibilidadeProdutoMapper.updateEntity(request, disponibilidadeProduto, produto);
        DisponibilidadeProduto disponibilidadeSalva = disponibilidadeProdutoRepository.save(disponibilidadeProduto);
        return disponibilidadeProdutoMapper.toResponse(disponibilidadeSalva);
    }

    @Transactional
    public void excluir(Long id) {
        DisponibilidadeProduto disponibilidadeProduto = buscarDisponibilidade(id);
        disponibilidadeProdutoRepository.delete(disponibilidadeProduto);
    }

    private DisponibilidadeProduto buscarDisponibilidade(Long id) {
        return disponibilidadeProdutoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disponibilidade nao encontrada"));
    }

    private Produto buscarProduto(Long produtoId) {
        return produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
    }

    private void validarQuantidade(DisponibilidadeProdutoRequest request) {
        if (request.quantidadeDisponivel() != null && request.quantidadeDisponivel() < 0) {
            throw new BusinessException("Quantidade disponivel nao pode ser negativa");
        }
    }

    private void validarPeriodo(DisponibilidadeProdutoRequest request) {
        if (request.dataInicial() != null
                && request.dataFinal() != null
                && !request.dataFinal().isAfter(request.dataInicial())) {
            throw new BusinessException("Data final deve ser maior que data inicial");
        }
    }

    private void validarProdutoSemDisponibilidade(Long produtoId) {
        if (disponibilidadeProdutoRepository.existsByProdutoId(produtoId)) {
            throw new BusinessException("Produto ja possui disponibilidade cadastrada");
        }
    }

    private void validarProdutoSemOutraDisponibilidade(Long produtoId, Long disponibilidadeId) {
        if (disponibilidadeProdutoRepository.existsByProdutoIdAndIdNot(produtoId, disponibilidadeId)) {
            throw new BusinessException("Produto ja possui disponibilidade cadastrada");
        }
    }
}
