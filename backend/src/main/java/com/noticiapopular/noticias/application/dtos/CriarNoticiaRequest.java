package com.noticiapopular.noticias.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarNoticiaRequest(
        @NotBlank(message = "Título é obrigatório")
        @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
        String titulo,

        @NotBlank(message = "Conteúdo é obrigatório")
        String conteudo,

        @Size(max = 500, message = "Resumo deve ter no máximo 500 caracteres")
        String resumo,

        String imagemUrl,

        @NotNull(message = "Categoria é obrigatória")
        String categoriaId,

        boolean publicarImediatamente
) {}
