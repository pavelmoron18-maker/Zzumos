package com.zzumos.service;

import com.zzumos.dto.Dtos.OpcionDto;
import com.zzumos.dto.Dtos.ProductoDto;
import com.zzumos.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProductoService {
    private final ProductoRepository productoRepo;

    public ProductoService(ProductoRepository productoRepo) {
        this.productoRepo = productoRepo;
    }

    @Transactional(readOnly = true)
    public List<ProductoDto> catalogo() {
        return productoRepo.findCatalogo().stream()
                .map(p -> new ProductoDto(
                        p.getIdProducto(), p.getNombre(), p.getDescripcion(), p.getPrecioBase(),
                        p.getOpciones().stream()
                                .filter(o -> o.isDisponible())
                                .map(o -> new OpcionDto(o.getIdOpcion(), o.getGrupo(),
                                        o.getNombre(), o.getPrecioAdicional()))
                                .toList()))
                .toList();
    }
}
