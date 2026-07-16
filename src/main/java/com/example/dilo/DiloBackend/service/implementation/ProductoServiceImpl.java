package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.request.ProductoRequestDTO;
import com.example.dilo.DiloBackend.dto.response.ProductoResponseDTO;
import com.example.dilo.DiloBackend.exception.ResourceNotFoundException;
import com.example.dilo.DiloBackend.model.Categoria;
import com.example.dilo.DiloBackend.model.Negocio;
import com.example.dilo.DiloBackend.model.Producto;
import com.example.dilo.DiloBackend.repository.CategoriaRepository;
import com.example.dilo.DiloBackend.repository.NegocioRepository;
import com.example.dilo.DiloBackend.repository.ProductoRepository;
import com.example.dilo.DiloBackend.service.ProductoService;
import com.example.dilo.DiloBackend.service.mapper.ProductoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final NegocioRepository negocioRepository;
    private final CategoriaRepository categoriaRepository;
    private final SupabaseStorageService storageService; // Tu servicio de subida de archivos
    private final ProductoMapper productoMapper;

    @Override
    public List<ProductoResponseDTO> obtenerPorNegocio(Long negocioId) {
        return productoRepository.findByNegocioId(negocioId).stream()
                .map(productoMapper::toDto)
                .toList();
    }

    @Override
    public List<ProductoResponseDTO> buscarPorTermino(Long negocioId, String term) {
        List<Producto> productos = productoRepository.buscarPorTermino(negocioId, term);

        if (productos.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron productos con el término: " + term);
        }

        return productos.stream()
                .map(productoMapper::toDto)
                .toList();
    }

    @Override
    public ProductoResponseDTO obtenerPorId(Long negocioId, Long id) {
        Producto producto = productoRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado en este negocio"));
        return productoMapper.toDto(producto);
    }

    @Override
    public ProductoResponseDTO crearProducto(Long negocioId, ProductoRequestDTO requestDTO, MultipartFile imagen) {
        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Negocio no encontrado"));

        if (productoRepository.existsByCodigoPrincipalAndNegocioId(requestDTO.getCodigoPrincipal(), negocioId)) {
            throw new RuntimeException("Ya existe un producto con el código " + requestDTO.getCodigoPrincipal() + " en su catálogo");
        }

        Categoria categoria = categoriaRepository.findByIdAndNegocioId(requestDTO.getCategoriaId(), negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("La categoría especificada no existe en su negocio"));

        Producto producto = productoMapper.toEntity(requestDTO);
        producto.setNegocio(negocio);
        producto.setCategoria(categoria);

        if (imagen != null && !imagen.isEmpty()) {
            try {
                String urlImagen = storageService.uploadFile(imagen, "productos"); // Se guardará en el bucket 'productos'
                producto.setImagen(urlImagen);
            } catch (Exception e) {
                throw new RuntimeException("Error al subir la imagen del producto: " + e.getMessage());
            }
        }

        Producto guardado = productoRepository.save(producto);
        return productoMapper.toDto(guardado);
    }

    @Override
    public ProductoResponseDTO actualizarProducto(Long negocioId, Long id, ProductoRequestDTO requestDTO, MultipartFile imagen) {
        Producto producto = productoRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado en este negocio"));

        if (!producto.getCodigoPrincipal().equals(requestDTO.getCodigoPrincipal()) &&
                productoRepository.existsByCodigoPrincipalAndNegocioId(requestDTO.getCodigoPrincipal(), negocioId)) {
            throw new RuntimeException("Ya existe otro producto con el código " + requestDTO.getCodigoPrincipal());
        }

        Categoria categoria = categoriaRepository.findByIdAndNegocioId(requestDTO.getCategoriaId(), negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("La categoría especificada no existe en su negocio"));

        producto.setCodigoPrincipal(requestDTO.getCodigoPrincipal());
        producto.setMarca(requestDTO.getMarca());
        producto.setNombre(requestDTO.getNombre());
        producto.setPrecioUnitario(requestDTO.getPrecioUnitario());
        producto.setGrabaIva(requestDTO.getGrabaIva());
        producto.setCategoria(categoria);

        if (imagen != null && !imagen.isEmpty()) {
            try {
                String urlImagen = storageService.uploadFile(imagen, "productos");
                producto.setImagen(urlImagen);
            } catch (Exception e) {
                throw new RuntimeException("Error al subir la nueva imagen: " + e.getMessage());
            }
        }

        Producto actualizado = productoRepository.save(producto);
        return productoMapper.toDto(actualizado);
    }

    @Override
    public void eliminarProducto(Long negocioId, Long id) {
        Producto producto = productoRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado en este negocio"));

        productoRepository.delete(producto);
    }
}