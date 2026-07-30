package br.com.finopaladar.backend.repository;

import br.com.finopaladar.backend.entity.StatusPedido;
import br.com.finopaladar.backend.entity.Pedido;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    boolean existsByCodigo(String codigo);

    long countByData(LocalDate data);

    long countByStatus(StatusPedido status);

    long countByStatusIn(Collection<StatusPedido> statuses);

    @Query("""
            SELECT SUM(pedido.valorTotal)
            FROM Pedido pedido
            WHERE pedido.data = :data
              AND pedido.status = :status
            """)
    BigDecimal sumValorTotalByDataAndStatus(
            @Param("data") LocalDate data,
            @Param("status") StatusPedido status
    );

    @EntityGraph(attributePaths = {"itens", "itens.produto", "enderecoEntrega"})
    Optional<Pedido> findByCodigo(String codigo);
}
