package com.noticiapopular.noticias.application.dtos;

import jakarta.validation.constraints.Size;

public record EditarNoticiaRequest(
        @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
        String titulo,

        String conteudo,

        @Size(max = 500, message = "Resumo deve ter no máximo 500 caracteres")
        String resumo,

        String imagemUrl
) {}
