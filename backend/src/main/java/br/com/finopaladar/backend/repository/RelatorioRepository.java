package br.com.finopaladar.backend.repository;

import br.com.finopaladar.backend.dto.CidadeMaisPedidosResponse;
import br.com.finopaladar.backend.dto.ProdutoVendidoResponse;
import br.com.finopaladar.backend.entity.Pedido;
import br.com.finopaladar.backend.entity.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface RelatorioRepository extends Repository<Pedido, Long> {

    @Query("""
            SELECT SUM(pedido.valorTotal)
            FROM Pedido pedido
            WHERE pedido.status = :status
              AND pedido.data BETWEEN :dataInicial AND :dataFinal
            """)
    BigDecimal sumValorTotalByStatusAndPeriodo(
            @Param("status") StatusPedido status,
            @Param("dataInicial") LocalDate dataInicial,
            @Param("dataFinal") LocalDate dataFinal
    );

    @Query("""
            SELECT COUNT(pedido)
            FROM Pedido pedido
            WHERE pedido.status = :status
              AND pedido.data BETWEEN :dataInicial AND :dataFinal
            """)
    long countPedidosByStatusAndPeriodo(
            @Param("status") StatusPedido status,
            @Param("dataInicial") LocalDate dataInicial,
            @Param("dataFinal") LocalDate dataFinal
    );

    @Query("""
            SELECT new br.com.finopaladar.backend.dto.ProdutoVendidoResponse(
                item.produto.id,
                item.nomeProduto,
                SUM(item.quantidade),
                SUM(item.subtotal)
            )
            FROM ItemPedido item
            WHERE item.pedido.status = :status
              AND item.pedido.data BETWEEN :dataInicial AND :dataFinal
            GROUP BY item.produto.id, item.nomeProduto
            ORDER BY SUM(item.quantidade) DESC, item.nomeProduto ASC
            """)
    List<ProdutoVendidoResponse> findProdutosMaisVendidosByStatusAndPeriodo(
            @Param("status") StatusPedido status,
            @Param("dataInicial") LocalDate dataInicial,
            @Param("dataFinal") LocalDate dataFinal,
            Pageable pageable
    );

    @Query("""
            SELECT new br.com.finopaladar.backend.dto.ProdutoVendidoResponse(
                item.produto.id,
                item.nomeProduto,
                SUM(item.quantidade),
                SUM(item.subtotal)
            )
            FROM ItemPedido item
            WHERE item.pedido.status = :status
              AND item.pedido.data BETWEEN :dataInicial AND :dataFinal
            GROUP BY item.produto.id, item.nomeProduto
            ORDER BY item.nomeProduto ASC
            """)
    List<ProdutoVendidoResponse> findQuantidadeVendidaPorProdutoByStatusAndPeriodo(
            @Param("status") StatusPedido status,
            @Param("dataInicial") LocalDate dataInicial,
            @Param("dataFinal") LocalDate dataFinal
    );

    @Query("""
            SELECT new br.com.finopaladar.backend.dto.CidadeMaisPedidosResponse(
                endereco.cidade,
                COUNT(pedido)
            )
            FROM Pedido pedido
            JOIN pedido.enderecoEntrega endereco
            WHERE pedido.status = :status
              AND pedido.data BETWEEN :dataInicial AND :dataFinal
            GROUP BY endereco.cidade
            ORDER BY COUNT(pedido) DESC, endereco.cidade ASC
            """)
    List<CidadeMaisPedidosResponse> findCidadesComMaisPedidosByStatusAndPeriodo(
            @Param("status") StatusPedido status,
            @Param("dataInicial") LocalDate dataInicial,
            @Param("dataFinal") LocalDate dataFinal,
            Pageable pageable
    );
}
