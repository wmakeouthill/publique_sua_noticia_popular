package com.noticiapopular.noticias.infrastructure.persistence.mappers;

import com.noticiapopular.noticias.domain.entities.Noticia;
import com.noticiapopular.noticias.infrastructure.persistence.entities.NoticiaEntity;
import org.springframework.stereotype.Component;

@Component
public class NoticiaMapper {

    public NoticiaEntity toEntity(Noticia noticia) {
        return NoticiaEntity.builder()
                .id(noticia.getId())
                .titulo(noticia.getTitulo())
                .conteudo(noticia.getConteudo())
                .resumo(noticia.getResumo())
                .imagemUrl(noticia.getImagemUrl())
                .categoriaId(noticia.getCategoriaId())
                .autorId(noticia.getAutorId())
                .status(noticia.getStatus())
                .visualizacoes(noticia.getVisualizacoes())
                .criadoEm(noticia.getCriadoEm())
                .atualizadoEm(noticia.getAtualizadoEm())
                .publicadoEm(noticia.getPublicadoEm())
                .build();
    }

    public Noticia toDomain(NoticiaEntity entity) {
        return Noticia.reconstituir(
                entity.getId(),
                entity.getTitulo(),
                entity.getConteudo(),
                entity.getResumo(),
                entity.getImagemUrl(),
                entity.getCategoriaId(),
                entity.getAutorId(),
                entity.getStatus(),
                entity.getVisualizacoes(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm(),
                entity.getPublicadoEm()
        );
    }
}
