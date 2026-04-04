package com.noticiapopular.comentarios.infrastructure.persistence.repositories;

import com.noticiapopular.comentarios.infrastructure.persistence.entities.ComentarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComentarioJpaRepository extends JpaRepository<ComentarioEntity, String> {

    List<ComentarioEntity> findByNoticiaIdOrderByCriadoEmAsc(String noticiaId);

    List<ComentarioEntity> findByNoticiaIdOrderByCriadoEmDesc(String noticiaId);

    @Query(value = """
            SELECT c.* FROM comentarios c
            LEFT JOIN (
              SELECT alvo_id, COUNT(*) AS total
              FROM reacoes WHERE alvo_tipo = 'COMENTARIO'
              GROUP BY alvo_id
            ) r ON c.id = r.alvo_id
            WHERE c.noticia_id = :noticiaId
            ORDER BY COALESCE(r.total, 0) DESC, c.criado_em ASC
            """, nativeQuery = true)
    List<ComentarioEntity> findByNoticiaIdOrderByLikes(@Param("noticiaId") String noticiaId);

    long countByNoticiaId(String noticiaId);
}
