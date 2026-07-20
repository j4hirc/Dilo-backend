package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.request.ProveedorRequestDTO;
import com.example.dilo.DiloBackend.dto.response.ProveedorResponseDTO;
import com.example.dilo.DiloBackend.exception.ResourceNotFoundException;
import com.example.dilo.DiloBackend.model.Categoria;
import com.example.dilo.DiloBackend.model.Negocio;
import com.example.dilo.DiloBackend.model.Proveedor;
import com.example.dilo.DiloBackend.repository.CategoriaRepository;
import com.example.dilo.DiloBackend.repository.NegocioRepository;
import com.example.dilo.DiloBackend.repository.ProveedorRepository;
import com.example.dilo.DiloBackend.service.ProveedorService;
import com.example.dilo.DiloBackend.service.mapper.ProveedorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final NegocioRepository negocioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorMapper proveedorMapper;

    @Override
    @Transactional(readOnly = true) // 🔥 SOLUCIÓN AL ERROR 500
    public List<ProveedorResponseDTO> getAllByNegocio(Long negocioId) {
        return proveedorRepository.findByNegocioId(negocioId).stream()
                .map(proveedorMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true) // 🔥 SOLUCIÓN AL ERROR 500
    public ProveedorResponseDTO getById(Long id, Long negocioId) {
        Proveedor proveedor = findProveedorAndValidateNegocio(id, negocioId);
        return proveedorMapper.toDto(proveedor);
    }

    @Override
    @Transactional
    public ProveedorResponseDTO create(Long negocioId, ProveedorRequestDTO requestDTO) {
        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Negocio no encontrado"));

        if (proveedorRepository.existsByDni(requestDTO.getDni())) {
            throw new RuntimeException("Ya existe un proveedor registrado con el DNI: " + requestDTO.getDni());
        }

        Proveedor proveedor = proveedorMapper.toEntity(requestDTO);
        proveedor.setNegocio(negocio);
        asignarCategorias(proveedor, requestDTO.getCategoriasIds());

        Proveedor guardado = proveedorRepository.save(proveedor);
        return proveedorMapper.toDto(guardado);
    }

    @Override
    @Transactional
    public ProveedorResponseDTO update(Long id, Long negocioId, ProveedorRequestDTO requestDTO) {
        Proveedor proveedor = findProveedorAndValidateNegocio(id, negocioId);

        // Si cambia el DNI, verificar que el nuevo no exista en otro proveedor
        if (!proveedor.getDni().equals(requestDTO.getDni()) && proveedorRepository.existsByDni(requestDTO.getDni())) {
            throw new RuntimeException("El DNI " + requestDTO.getDni() + " ya está en uso por otro proveedor.");
        }

        proveedor.setDni(requestDTO.getDni());
        proveedor.setNombre(requestDTO.getNombre());
        proveedor.setTelefono(requestDTO.getTelefono());

        if (requestDTO.getEstado() != null) {
            proveedor.setEstado(requestDTO.getEstado());
        }

        asignarCategorias(proveedor, requestDTO.getCategoriasIds());

        Proveedor actualizado = proveedorRepository.save(proveedor);
        return proveedorMapper.toDto(actualizado);
    }

    @Override
    @Transactional
    public void delete(Long id, Long negocioId) {
        Proveedor proveedor = findProveedorAndValidateNegocio(id, negocioId);
        proveedorRepository.delete(proveedor);
    }

    // Métodos Auxiliares
    private Proveedor findProveedorAndValidateNegocio(Long id, Long negocioId) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        if (!proveedor.getNegocio().getId().equals(negocioId)) {
            throw new RuntimeException("El proveedor no pertenece a este negocio");
        }
        return proveedor;
    }

    private void asignarCategorias(Proveedor proveedor, List<Long> categoriasIds) {
        if (categoriasIds != null && !categoriasIds.isEmpty()) {
            List<Categoria> categoriasEncontradas = categoriaRepository.findAllById(categoriasIds);
            if (categoriasEncontradas.size() != categoriasIds.size()) {
                throw new ResourceNotFoundException("Una o más categorías proporcionadas no existen");
            }
            proveedor.setCategorias(new HashSet<>(categoriasEncontradas));
        } else {
            proveedor.setCategorias(new HashSet<>());
        }
    }
}