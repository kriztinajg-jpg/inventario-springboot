package co.edu.cesde.inventarionoche.application.service;

import co.edu.cesde.inventarionoche.domain.model.Producto;

import java.util.List;

public interface ProductoService {

    Producto save(Producto producto);

    List<Producto> findAll();

    Producto findById(Long id);

    void delete(Long id);

    Producto update(Producto producto);
}
