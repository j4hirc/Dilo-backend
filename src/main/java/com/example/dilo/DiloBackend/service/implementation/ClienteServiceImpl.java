package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.request.ClienteRequestDTO;
import com.example.dilo.DiloBackend.dto.response.ClienteResponseDTO;
import com.example.dilo.DiloBackend.exception.ResourceNotFoundException;
import com.example.dilo.DiloBackend.model.Cliente;
import com.example.dilo.DiloBackend.model.Negocio;
import com.example.dilo.DiloBackend.repository.ClienteRepository;
import com.example.dilo.DiloBackend.repository.NegocioRepository;
import com.example.dilo.DiloBackend.service.ClienteService;
import com.example.dilo.DiloBackend.service.mapper.ClienteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final NegocioRepository negocioRepository;
    private final ClienteMapper clienteMapper;

    @Override
    public List<ClienteResponseDTO> obtenerPorNegocio(Long negocioId) {
        return clienteRepository.findByNegocioId(negocioId).stream()
                .map(clienteMapper::toDto)
                .toList();
    }

    @Override
    public ClienteResponseDTO obtenerPorId(Long negocioId, Long id) {
        Cliente cliente = clienteRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        return clienteMapper.toDto(cliente);
    }

    @Override
    public ClienteResponseDTO crearCliente(Long negocioId, ClienteRequestDTO requestDTO) {
        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Negocio no encontrado"));

        if (clienteRepository.existsByDniAndNegocioId(requestDTO.getDni(), negocioId)) {
            throw new RuntimeException("Ya existe un cliente con el DNI " + requestDTO.getDni() + " en este negocio");
        }

        Cliente cliente = clienteMapper.toEntity(requestDTO);
        cliente.setNegocio(negocio);

        Cliente guardado = clienteRepository.save(cliente);
        return clienteMapper.toDto(guardado);
    }

    @Override
    public ClienteResponseDTO actualizarCliente(Long negocioId, Long id, ClienteRequestDTO requestDTO) {
        Cliente cliente = clienteRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        if (!cliente.getDni().equals(requestDTO.getDni()) &&
                clienteRepository.existsByDniAndNegocioId(requestDTO.getDni(), negocioId)) {
            throw new RuntimeException("Ya existe otro cliente registrado con ese DNI");
        }

        cliente.setDni(requestDTO.getDni());
        cliente.setPrimerNombre(requestDTO.getPrimerNombre());
        cliente.setSegundoNombre(requestDTO.getSegundoNombre());
        cliente.setApellidoPaterno(requestDTO.getApellidoPaterno());
        cliente.setApellidoMaterno(requestDTO.getApellidoMaterno());
        cliente.setEmail(requestDTO.getEmail());
        cliente.setFechaNacimiento(requestDTO.getFechaNacimiento());
        cliente.setTelefono(requestDTO.getTelefono());
        cliente.setDireccion(requestDTO.getDireccion());

        Cliente actualizado = clienteRepository.save(cliente);
        return clienteMapper.toDto(actualizado);
    }

    @Override
    public void eliminarCliente(Long negocioId, Long id) {
        Cliente cliente = clienteRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        clienteRepository.delete(cliente);
    }

    @Override
    public List<ClienteResponseDTO> buscarPorTermino(Long negocioId, String term) {
        List<Cliente> clientes = clienteRepository.buscarPorTermino(negocioId, term);

        if (clientes.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron clientes que coincidan con la búsqueda");
        }

        return clientes.stream()
                .map(clienteMapper::toDto)
                .toList();
    }
}