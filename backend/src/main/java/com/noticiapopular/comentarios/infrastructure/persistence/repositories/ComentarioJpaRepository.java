package com.noticiapopular.comentarios.infrastructure.persistence.repositories;

import com.noticiapopular.comentarios.infrastructure.persistence.entities.ComentarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComentarioJpaRepository extends JpaRepository<ComentarioEntity, String> {

    List<ComentarioEntity> findByNoticiaIdOrderByCriadoEmAsc(String noticiaId);

    long countByNoticiaId(String noticiaId);
}
