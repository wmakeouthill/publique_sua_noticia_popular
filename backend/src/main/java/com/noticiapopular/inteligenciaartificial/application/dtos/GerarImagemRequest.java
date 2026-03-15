package com.noticiapopular.inteligenciaartificial.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GerarImagemRequest(
        @NotBlank(message = "Prompt é obrigatório")
        @Size(max = 1000, message = "Prompt deve ter no máximo 1000 caracteres")
        String prompt
) {}
