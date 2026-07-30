package br.com.finopaladar.backend.mapper;

import br.com.finopaladar.backend.dto.ProdutoRequest;
import br.com.finopaladar.backend.dto.ProdutoPublicoResponse;
import br.com.finopaladar.backend.dto.ProdutoResponse;
import br.com.finopaladar.backend.entity.Produto;
import org.springframework.stereotype.Component;

@Component
public class ProdutoMapper {

    public Produto toEntity(ProdutoRequest request) {
        Produto produto = new Produto();
        updateEntity(request, produto);
        return produto;
    }

    public ProdutoResponse toResponse(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getPesoMedioGramas(),
                produto.getImagem(),
                produto.getAtivo()
        );
    }

    public ProdutoPublicoResponse toPublicoResponse(Produto produto) {
        return new ProdutoPublicoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getPreco(),
                produto.getPesoMedioGramas(),
                produto.getImagem(),
                produto.getDisponibilidadeProduto().getQuantidadeDisponivel()
        );
    }

    public void updateEntity(ProdutoRequest request, Produto produto) {
        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setPreco(request.preco());
        produto.setPesoMedioGramas(request.pesoMedioGramas());
        produto.setImagem(request.imagem());
        produto.setAtivo(request.ativo());
    }
}
