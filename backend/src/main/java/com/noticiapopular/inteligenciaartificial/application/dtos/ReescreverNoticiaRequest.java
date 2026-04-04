package com.noticiapopular.inteligenciaartificial.application.dtos;

import jakarta.validation.constraints.NotBlank;

public record ReescreverNoticiaRequest(
        @NotBlank(message = "Título é obrigatório")
        String titulo,

        @NotBlank(message = "Conteúdo é obrigatório")
        String conteudo
) {}
