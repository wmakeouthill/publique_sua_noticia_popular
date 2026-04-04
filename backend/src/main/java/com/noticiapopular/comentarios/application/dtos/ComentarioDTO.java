package com.noticiapopular.comentarios.application.dtos;

import com.noticiapopular.comentarios.domain.entities.Comentario;

import java.time.Instant;

public record ComentarioDTO(
        String id,
        String noticiaId,
        String autorId,
        String autorNome,
        String autorAvatarUrl,
        String conteudo,
        Instant criadoEm
) {
    public static ComentarioDTO from(Comentario comentario, String autorNome, String autorAvatarUrl) {
        return new ComentarioDTO(
                comentario.getId(),
                comentario.getNoticiaId(),
                comentario.getAutorId(),
                autorNome,
                autorAvatarUrl,
                comentario.getConteudo(),
                comentario.getCriadoEm()
        );
    }
}
