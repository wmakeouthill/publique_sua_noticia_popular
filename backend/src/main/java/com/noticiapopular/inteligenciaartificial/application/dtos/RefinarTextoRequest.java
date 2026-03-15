package com.noticiapopular.inteligenciaartificial.application.dtos;

import jakarta.validation.constraints.NotBlank;

public record RefinarTextoRequest(
        @NotBlank(message = "Texto atual é obrigatório")
        String textoAtual,

        @NotBlank(message = "Instrução de refinamento é obrigatória")
        String instrucao
) {}
