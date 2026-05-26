package com.rodolfogodinez.kinalapp.repository;

import com.rodolfogodinez.kinalapp.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, String> {
    List<Cliente> findByEstado(Integer estado);
}
