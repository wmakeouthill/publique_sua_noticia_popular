package com.noticiapopular.noticias.application.dtos;

import com.noticiapopular.noticias.domain.entities.Noticia;

import java.time.Instant;

public record NoticiaDTO(
        String id,
        String titulo,
        String conteudo,
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
        Instant atualizadoEm,
        Instant publicadoEm
) {
    public static NoticiaDTO from(Noticia noticia, String autorNome, String autorAvatarUrl,
                                   long totalLikes, boolean likedByMe) {
        return new NoticiaDTO(
                noticia.getId(),
                noticia.getTitulo(),
                noticia.getConteudo(),
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
                noticia.getAtualizadoEm(),
                noticia.getPublicadoEm()
        );
    }
}
