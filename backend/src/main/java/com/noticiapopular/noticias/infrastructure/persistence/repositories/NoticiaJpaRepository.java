package com.noticiapopular.noticias.infrastructure.persistence.repositories;

import com.noticiapopular.noticias.domain.valueobjects.StatusNoticia;
import com.noticiapopular.noticias.infrastructure.persistence.entities.NoticiaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticiaJpaRepository extends JpaRepository<NoticiaEntity, String> {

        @Query("""
                        SELECT n FROM NoticiaEntity n
                        WHERE n.status = 'PUBLICADA'
                        AND (:categoriaId IS NULL OR n.categoriaId = :categoriaId)
                        """)
        Page<NoticiaEntity> findPublicadasSemBusca(
                        @Param("categoriaId") String categoriaId,
                        Pageable pageable);

        @Query("""
                        SELECT n FROM NoticiaEntity n
                        WHERE n.status = 'PUBLICADA'
                        AND (:categoriaId IS NULL OR n.categoriaId = :categoriaId)
                        AND LOWER(n.titulo) LIKE LOWER(CONCAT('%', :busca, '%'))
                        """)
        Page<NoticiaEntity> findPublicadasComBusca(
                        @Param("categoriaId") String categoriaId,
                        @Param("busca") String busca,
                        Pageable pageable);

        Page<NoticiaEntity> findByAutorIdOrderByCriadoEmDesc(String autorId, Pageable pageable);

        long countByStatus(StatusNoticia status);

        long countByCategoriaId(String categoriaId);
}
