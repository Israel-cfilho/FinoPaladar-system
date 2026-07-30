package br.com.finopaladar.backend.repository;

import br.com.finopaladar.backend.entity.Produto;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findAllByOrderByNomeAsc();

    List<Produto> findAllByAtivoTrueOrderByNomeAsc();

    Optional<Produto> findByIdAndAtivoTrue(Long id);

    @Query("""
            SELECT produto
            FROM Produto produto
            JOIN FETCH produto.disponibilidadeProduto disponibilidade
            WHERE produto.ativo = true
              AND disponibilidade.quantidadeDisponivel > 0
              AND disponibilidade.dataInicial <= :dataReferencia
              AND disponibilidade.dataFinal >= :dataReferencia
            ORDER BY produto.nome ASC
            """)
    List<Produto> findProdutosDisponiveisParaCatalogo(@Param("dataReferencia") LocalDate dataReferencia);

    @Query("""
            SELECT produto
            FROM Produto produto
            JOIN FETCH produto.disponibilidadeProduto disponibilidade
            WHERE produto.id = :id
              AND produto.ativo = true
              AND disponibilidade.quantidadeDisponivel > 0
              AND disponibilidade.dataInicial <= :dataReferencia
              AND disponibilidade.dataFinal >= :dataReferencia
            """)
    Optional<Produto> findProdutoDisponivelParaCatalogoPorId(
            @Param("id") Long id,
            @Param("dataReferencia") LocalDate dataReferencia
    );
}
