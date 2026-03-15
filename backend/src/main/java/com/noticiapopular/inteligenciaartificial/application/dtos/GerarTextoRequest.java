package com.noticiapopular.inteligenciaartificial.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GerarTextoRequest(
        @NotBlank(message = "Prompt é obrigatório")
        @Size(max = 2000, message = "Prompt deve ter no máximo 2000 caracteres")
        String prompt,

        String categoriaHint
) {}
