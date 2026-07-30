package br.com.finopaladar.backend.repository;

import br.com.finopaladar.backend.entity.Administrador;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

    Optional<Administrador> findByEmailIgnoreCaseAndAtivoTrue(String email);
}
