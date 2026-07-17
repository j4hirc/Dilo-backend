package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.ParametroGlobal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParametroGlobalRepository extends JpaRepository<ParametroGlobal, String> {
}