package br.com.finopaladar.backend.repository;

import br.com.finopaladar.backend.entity.DisponibilidadeProduto;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DisponibilidadeProdutoRepository extends JpaRepository<DisponibilidadeProduto, Long> {

    @EntityGraph(attributePaths = "produto")
    List<DisponibilidadeProduto> findAllByOrderByDataInicialAsc();

    @Override
    @EntityGraph(attributePaths = "produto")
    Optional<DisponibilidadeProduto> findById(Long id);

    boolean existsByProdutoId(Long produtoId);

    boolean existsByProdutoIdAndIdNot(Long produtoId, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT disponibilidade
            FROM DisponibilidadeProduto disponibilidade
            WHERE disponibilidade.produto.id = :produtoId
            """)
    Optional<DisponibilidadeProduto> findByProdutoIdComLock(@Param("produtoId") Long produtoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT disponibilidade
            FROM DisponibilidadeProduto disponibilidade
            WHERE disponibilidade.produto.id = :produtoId
              AND disponibilidade.dataInicial <= :dataReferencia
              AND disponibilidade.dataFinal >= :dataReferencia
            """)
    Optional<DisponibilidadeProduto> findDisponibilidadeVigenteComLock(
            @Param("produtoId") Long produtoId,
            @Param("dataReferencia") LocalDate dataReferencia
    );
}
