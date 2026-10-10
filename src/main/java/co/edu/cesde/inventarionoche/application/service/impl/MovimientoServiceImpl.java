package co.edu.cesde.inventarionoche.application.service.impl;

import co.edu.cesde.inventarionoche.application.service.MovimientoService;
import co.edu.cesde.inventarionoche.domain.model.Movimiento;
import co.edu.cesde.inventarionoche.domain.model.Producto;
import co.edu.cesde.inventarionoche.domain.model.TipoMovimiento;
import co.edu.cesde.inventarionoche.infrastructure.exception.InsufficientStockException;
import co.edu.cesde.inventarionoche.infrastructure.exception.ResourceNotFoundException;
import co.edu.cesde.inventarionoche.infrastructure.exception.ValidationException;
import co.edu.cesde.inventarionoche.infrastructure.repository.MovimientoRepository;
import co.edu.cesde.inventarionoche.infrastructure.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional
    public Movimiento save(Movimiento movimiento) {
        Producto producto = buscarProducto(movimiento);

        aplicarMovimiento(producto, movimiento.getTipo(), movimiento.getCantidad());

        productoRepository.save(producto);
        movimiento.setId(null);
        movimiento.setProducto(producto);
        return movimientoRepository.save(movimiento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Movimiento> findAll() {
        return movimientoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Movimiento findById(Long id) {
        return movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el movimiento con id: " + id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Movimiento movimiento = findById(id);
        movimientoRepository.delete(movimiento);
    }

    @Override
    @Transactional
    public Movimiento update(Movimiento movimiento) {
        if (movimiento.getId() == null) {
            throw new ValidationException("El id del movimiento es obligatorio para actualizar");
        }

        Movimiento existente = findById(movimiento.getId());

        // Primero se deshace el efecto del movimiento anterior sobre el stock
        Producto productoAnterior = existente.getProducto();
        revertirMovimiento(productoAnterior, existente.getTipo(), existente.getCantidad());
        productoRepository.save(productoAnterior);

        // Despues se aplica el nuevo movimiento
        Producto productoNuevo = buscarProducto(movimiento);
        aplicarMovimiento(productoNuevo, movimiento.getTipo(), movimiento.getCantidad());
        productoRepository.save(productoNuevo);

        existente.setProducto(productoNuevo);
        existente.setCantidad(movimiento.getCantidad());
        existente.setTipo(movimiento.getTipo());
        return movimientoRepository.save(existente);
    }

    // ---------- metodos auxiliares ----------

    private Producto buscarProducto(Movimiento movimiento) {
        if (movimiento.getProducto() == null || movimiento.getProducto().getId() == null) {
            throw new ValidationException("Debe indicar el id del producto del movimiento");
        }
        Long productoId = movimiento.getProducto().getId();
        return productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el producto con id: " + productoId));
    }

    private void aplicarMovimiento(Producto producto, TipoMovimiento tipo, int cantidad) {
        if (tipo == TipoMovimiento.ENTRADA) {
            producto.sumarStock(cantidad);
        } else {
            if (producto.getStock() < cantidad) {
                throw new InsufficientStockException(
                        "Stock insuficiente para el producto '" + producto.getNombre()
                                + "'. Disponible: " + producto.getStock()
                                + ", solicitado: " + cantidad);
            }
            producto.restarStock(cantidad);
        }
    }

    private void revertirMovimiento(Producto producto, TipoMovimiento tipo, int cantidad) {
        if (tipo == TipoMovimiento.ENTRADA) {
            if (producto.getStock() < cantidad) {
                throw new InsufficientStockException(
                        "No se puede modificar el movimiento: el stock actual de '"
                                + producto.getNombre() + "' ya no alcanza para revertir la entrada");
            }
            producto.restarStock(cantidad);
        } else {
            producto.sumarStock(cantidad);
        }
    }
}
