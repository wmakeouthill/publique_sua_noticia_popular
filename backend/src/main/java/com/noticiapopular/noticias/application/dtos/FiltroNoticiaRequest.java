package com.noticiapopular.noticias.application.dtos;

public record FiltroNoticiaRequest(
        String categoriaId,
        String busca
) {}
