package com.noticiapopular.categorias.infrastructure.persistence.repositories;

import com.noticiapopular.categorias.infrastructure.persistence.entities.CategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaJpaRepository extends JpaRepository<CategoriaEntity, String> {

    List<CategoriaEntity> findByAtivaOrderByOrdemAsc(boolean ativa);

    List<CategoriaEntity> findAllByOrderByOrdemAsc();

    Optional<CategoriaEntity> findBySlug(String slug);

    boolean existsByNomeIgnoreCase(String nome);
}
