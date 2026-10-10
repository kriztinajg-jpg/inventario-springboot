package co.edu.cesde.inventarionoche.application.service.impl;

import co.edu.cesde.inventarionoche.application.service.ProductoService;
import co.edu.cesde.inventarionoche.domain.model.Producto;
import co.edu.cesde.inventarionoche.infrastructure.exception.DuplicateResourceException;
import co.edu.cesde.inventarionoche.infrastructure.exception.ResourceNotFoundException;
import co.edu.cesde.inventarionoche.infrastructure.exception.ValidationException;
import co.edu.cesde.inventarionoche.infrastructure.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

    @Override
    @Transactional
    public Producto save(Producto producto) {
        // Validar que no exista otro producto con el mismo nombre
        if (productoRepository.findByNombre(producto.getNombre()).isPresent()) {
            throw new DuplicateResourceException(
                    "Ya existe un producto con el nombre: " + producto.getNombre());
        }
        // Por si viene un id en el JSON, lo ponemos en null para que siempre cree uno nuevo
        producto.setId(null);
        return productoRepository.save(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Producto findById(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el producto con id: " + id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Producto producto = findById(id);
        productoRepository.delete(producto);
    }

    @Override
    @Transactional
    public Producto update(Producto producto) {
        if (producto.getId() == null) {
            throw new ValidationException("El id del producto es obligatorio para actualizar");
        }

        // Si no existe lanza ResourceNotFoundException
        findById(producto.getId());

        // Validar que el nombre no lo tenga otro producto distinto
        productoRepository.findByNombre(producto.getNombre()).ifPresent(existente -> {
            if (!existente.getId().equals(producto.getId())) {
                throw new DuplicateResourceException(
                        "Ya existe un producto con el nombre: " + producto.getNombre());
            }
        });

        return productoRepository.save(producto);
    }
}
