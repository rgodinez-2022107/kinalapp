package com.rodolfogodinez.kinalapp.service;

import com.rodolfogodinez.kinalapp.entity.Cliente;
import com.rodolfogodinez.kinalapp.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClienteService implements IClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listarActivos() {
        return clienteRepository.findByEstado(1);
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        validarCliente(cliente);
        if (cliente.getEstado() == null || cliente.getEstado() == 0) {
            cliente.setEstado(1);
        }
        return clienteRepository.save(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorDPI(String dpi) {
        return clienteRepository.findById(dpi);
    }

    @Override
    public Cliente actualizar(String dpi, Cliente cliente) {
        if (!clienteRepository.existsById(dpi)) {
            throw new RuntimeException("Cliente no encontrado con DPI: " + dpi);
        }
        cliente.setDPICliente(dpi);
        validarCliente(cliente);
        return clienteRepository.save(cliente);
    }

    @Override
    public void eliminar(String dpi) {
        if (!clienteRepository.existsById(dpi)) {
            throw new RuntimeException("Cliente no encontrado con DPI: " + dpi);
        }
        clienteRepository.deleteById(dpi);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorDPI(String dpi) {
        return clienteRepository.existsById(dpi);
    }

    private void validarCliente(Cliente cliente) {
        if (cliente.getDPICliente() == null || cliente.getDPICliente().trim().isEmpty()) {
            throw new IllegalArgumentException("El DPI es obligatorio");
        }
        if (cliente.getNombreCliente() == null || cliente.getNombreCliente().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (cliente.getApellidoCliente() == null || cliente.getApellidoCliente().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }
    }
}
