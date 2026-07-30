package br.com.finopaladar.backend.service;

import br.com.finopaladar.backend.dto.ProdutoRequest;
import br.com.finopaladar.backend.dto.ProdutoPublicoResponse;
import br.com.finopaladar.backend.dto.ProdutoResponse;
import br.com.finopaladar.backend.entity.Produto;
import br.com.finopaladar.backend.exception.ResourceNotFoundException;
import br.com.finopaladar.backend.mapper.ProdutoMapper;
import br.com.finopaladar.backend.repository.ProdutoRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;
    private final ImagemProdutoStorageService imagemProdutoStorageService;

    public ProdutoService(
            ProdutoRepository produtoRepository,
            ProdutoMapper produtoMapper,
            ImagemProdutoStorageService imagemProdutoStorageService
    ) {
        this.produtoRepository = produtoRepository;
        this.produtoMapper = produtoMapper;
        this.imagemProdutoStorageService = imagemProdutoStorageService;
    }

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        Produto produto = produtoMapper.toEntity(request);
        Produto produtoSalvo = produtoRepository.save(produto);
        return produtoMapper.toResponse(produtoSalvo);
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarTodos() {
        return produtoRepository.findAllByOrderByNomeAsc()
                .stream()
                .map(produtoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProdutoPublicoResponse> listarProdutosPublicos() {
        return produtoRepository.findProdutosDisponiveisParaCatalogo(LocalDate.now())
                .stream()
                .map(produtoMapper::toPublicoResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Long id) {
        Produto produto = buscarEntidadePorId(id);
        return produtoMapper.toResponse(produto);
    }

    @Transactional(readOnly = true)
    public ProdutoPublicoResponse buscarProdutoPublicoPorId(Long id) {
        Produto produto = produtoRepository.findProdutoDisponivelParaCatalogoPorId(id, LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
        return produtoMapper.toPublicoResponse(produto);
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto produto = buscarEntidadePorId(id);
        produtoMapper.updateEntity(request, produto);
        Produto produtoSalvo = produtoRepository.save(produto);
        return produtoMapper.toResponse(produtoSalvo);
    }

    @Transactional
    public ProdutoResponse atualizarImagem(Long id, MultipartFile imagem) {
        Produto produto = buscarEntidadePorId(id);
        String urlImagem = imagemProdutoStorageService.salvar(imagem);
        produto.setImagem(urlImagem);
        Produto produtoSalvo = produtoRepository.save(produto);
        return produtoMapper.toResponse(produtoSalvo);
    }

    @Transactional
    public void excluir(Long id) {
        Produto produto = buscarEntidadePorId(id);
        produto.setAtivo(Boolean.FALSE);
        produtoRepository.save(produto);
    }

    private Produto buscarEntidadePorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
    }
}
