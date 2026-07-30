package br.com.finopaladar.backend.repository;

import br.com.finopaladar.backend.entity.HistoricoStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoStatusRepository extends JpaRepository<HistoricoStatus, Long> {

    List<HistoricoStatus> findByPedidoCodigoOrderByDataHoraAsc(String codigo);
}
