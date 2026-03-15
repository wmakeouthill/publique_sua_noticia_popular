package com.noticiapopular.noticias.application.dtos;

import com.noticiapopular.autenticacao.application.dtos.PerfilUsuarioDTO;
import com.noticiapopular.categorias.application.dtos.CategoriaDTO;
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
        String status,
        long visualizacoes,
        Instant criadoEm,
        Instant atualizadoEm,
        Instant publicadoEm
) {
    public static NoticiaDTO from(Noticia noticia) {
        return new NoticiaDTO(
                noticia.getId(),
                noticia.getTitulo(),
                noticia.getConteudo(),
                noticia.getResumo(),
                noticia.getImagemUrl(),
                noticia.getCategoriaId(),
                noticia.getAutorId(),
                noticia.getStatus().name(),
                noticia.getVisualizacoes(),
                noticia.getCriadoEm(),
                noticia.getAtualizadoEm(),
                noticia.getPublicadoEm()
        );
    }
}
