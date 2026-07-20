package com.example.dilo.DiloBackend.service.mapper;

import com.example.dilo.DiloBackend.dto.request.ProductoRequestDTO;
import com.example.dilo.DiloBackend.dto.response.ProductoResponseDTO;
import com.example.dilo.DiloBackend.model.Producto;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    public ProductoResponseDTO toDto(Producto producto) {
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(producto.getId());

        if (producto.getCategoria() != null) {
            dto.setCategoriaId(producto.getCategoria().getId());
            dto.setCategoriaNombre(producto.getCategoria().getNombre());
        }

        dto.setCodigoPrincipal(producto.getCodigoPrincipal());
        dto.setMarca(producto.getMarca());
        dto.setImagen(producto.getImagen());
        dto.setNombre(producto.getNombre());
        dto.setPrecioUnitario(producto.getPrecioUnitario());
        dto.setGrabaIva(producto.getGrabaIva());

        // --- NUEVOS CAMPOS MAPEADOS ---
        dto.setUnidadMedida(producto.getUnidadMedida());
        dto.setTieneCaducidad(producto.getTieneCaducidad());

        return dto;
    }

    public Producto toEntity(ProductoRequestDTO dto) {
        Producto producto = new Producto();
        producto.setCodigoPrincipal(dto.getCodigoPrincipal());
        producto.setMarca(dto.getMarca());
        producto.setNombre(dto.getNombre());
        producto.setPrecioUnitario(dto.getPrecioUnitario());
        producto.setGrabaIva(dto.getGrabaIva());

        producto.setUnidadMedida(dto.getUnidadMedida());
        producto.setTieneCaducidad(dto.getTieneCaducidad());

        return producto;
    }
}