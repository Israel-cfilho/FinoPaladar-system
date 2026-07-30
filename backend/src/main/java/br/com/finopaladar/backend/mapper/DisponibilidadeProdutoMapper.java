package br.com.finopaladar.backend.mapper;

import br.com.finopaladar.backend.dto.DisponibilidadeProdutoRequest;
import br.com.finopaladar.backend.dto.DisponibilidadeProdutoResponse;
import br.com.finopaladar.backend.entity.DisponibilidadeProduto;
import br.com.finopaladar.backend.entity.Produto;
import org.springframework.stereotype.Component;

@Component
public class DisponibilidadeProdutoMapper {

    public DisponibilidadeProduto toEntity(DisponibilidadeProdutoRequest request, Produto produto) {
        DisponibilidadeProduto disponibilidadeProduto = new DisponibilidadeProduto();
        updateEntity(request, disponibilidadeProduto, produto);
        return disponibilidadeProduto;
    }

    public void updateEntity(
            DisponibilidadeProdutoRequest request,
            DisponibilidadeProduto disponibilidadeProduto,
            Produto produto
    ) {
        disponibilidadeProduto.setProduto(produto);
        disponibilidadeProduto.setQuantidadeDisponivel(request.quantidadeDisponivel());
        disponibilidadeProduto.setDataInicial(request.dataInicial());
        disponibilidadeProduto.setDataFinal(request.dataFinal());
    }

    public DisponibilidadeProdutoResponse toResponse(DisponibilidadeProduto disponibilidadeProduto) {
        Produto produto = disponibilidadeProduto.getProduto();
        return new DisponibilidadeProdutoResponse(
                disponibilidadeProduto.getId(),
                produto.getId(),
                produto.getNome(),
                disponibilidadeProduto.getQuantidadeDisponivel(),
                disponibilidadeProduto.getDataInicial(),
                disponibilidadeProduto.getDataFinal()
        );
    }
}
