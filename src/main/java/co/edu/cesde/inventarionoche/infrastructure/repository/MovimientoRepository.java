package co.edu.cesde.inventarionoche.infrastructure.repository;

import co.edu.cesde.inventarionoche.domain.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
}
