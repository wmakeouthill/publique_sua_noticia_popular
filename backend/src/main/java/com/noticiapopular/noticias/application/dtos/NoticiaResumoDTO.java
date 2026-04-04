package com.noticiapopular.noticias.application.dtos;

import com.noticiapopular.noticias.domain.entities.Noticia;

import java.time.Instant;

public record NoticiaResumoDTO(
        String id,
        String titulo,
        String resumo,
        String imagemUrl,
        String categoriaId,
        String autorId,
        String autorNome,
        String autorAvatarUrl,
        String status,
        long visualizacoes,
        long totalLikes,
        boolean likedByMe,
        Instant criadoEm,
        Instant publicadoEm
) {
    public static NoticiaResumoDTO from(Noticia noticia, String autorNome, String autorAvatarUrl,
                                        long totalLikes, boolean likedByMe) {
        return new NoticiaResumoDTO(
                noticia.getId(),
                noticia.getTitulo(),
                noticia.getResumo(),
                noticia.getImagemUrl(),
                noticia.getCategoriaId(),
                noticia.getAutorId(),
                autorNome,
                autorAvatarUrl,
                noticia.getStatus().name(),
                noticia.getVisualizacoes(),
                totalLikes,
                likedByMe,
                noticia.getCriadoEm(),
                noticia.getPublicadoEm()
        );
    }
}
