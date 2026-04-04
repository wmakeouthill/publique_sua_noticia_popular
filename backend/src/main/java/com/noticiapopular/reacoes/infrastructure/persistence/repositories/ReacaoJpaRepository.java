package com.noticiapopular.reacoes.infrastructure.persistence.repositories;

import com.noticiapopular.reacoes.domain.valueobjects.AlvoTipo;
import com.noticiapopular.reacoes.infrastructure.persistence.entities.ReacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ReacaoJpaRepository extends JpaRepository<ReacaoEntity, String> {

    boolean existsByUsuarioIdAndAlvoTipoAndAlvoId(String usuarioId, AlvoTipo alvoTipo, String alvoId);

    @Modifying
    @Query("DELETE FROM ReacaoEntity r WHERE r.usuarioId = :uid AND r.alvoTipo = :tipo AND r.alvoId = :alvoId")
    void deleteByUsuarioIdAndAlvoTipoAndAlvoId(
            @Param("uid") String usuarioId,
            @Param("tipo") AlvoTipo alvoTipo,
            @Param("alvoId") String alvoId);

    long countByAlvoTipoAndAlvoId(AlvoTipo alvoTipo, String alvoId);

    @Query("""
            SELECT r.alvoId, COUNT(r)
            FROM ReacaoEntity r
            WHERE r.alvoTipo = :tipo AND r.alvoId IN :ids
            GROUP BY r.alvoId
            """)
    List<Object[]> countGroupedByAlvoId(@Param("tipo") AlvoTipo tipo, @Param("ids") Collection<String> ids);

    @Query("""
            SELECT r.alvoId
            FROM ReacaoEntity r
            WHERE r.usuarioId = :uid AND r.alvoTipo = :tipo AND r.alvoId IN :ids
            """)
    List<String> findAlvoIdsLikedByUsuario(
            @Param("uid") String usuarioId,
            @Param("tipo") AlvoTipo tipo,
            @Param("ids") Collection<String> ids);
}
