package com.noticiapopular.inteligenciaartificial.application.dtos;

import jakarta.validation.constraints.NotBlank;

public record MelhorarTituloRequest(
        @NotBlank(message = "Título atual é obrigatório")
        String tituloAtual,

        String conteudoResumo
) {}
