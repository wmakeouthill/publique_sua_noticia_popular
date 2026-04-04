package com.noticiapopular.comentarios.infrastructure.persistence.mappers;

import com.noticiapopular.comentarios.domain.entities.Comentario;
import com.noticiapopular.comentarios.infrastructure.persistence.entities.ComentarioEntity;
import org.springframework.stereotype.Component;

@Component
public class ComentarioMapper {

    public ComentarioEntity toEntity(Comentario comentario) {
        return ComentarioEntity.builder()
                .id(comentario.getId())
                .noticiaId(comentario.getNoticiaId())
                .autorId(comentario.getAutorId())
                .conteudo(comentario.getConteudo())
                .criadoEm(comentario.getCriadoEm())
                .build();
    }

    public Comentario toDomain(ComentarioEntity entity) {
        return Comentario.reconstituir(
                entity.getId(),
                entity.getNoticiaId(),
                entity.getAutorId(),
                entity.getConteudo(),
                entity.getCriadoEm()
        );
    }
}
