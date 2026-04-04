package com.noticiapopular.inteligenciaartificial.application.dtos;

import jakarta.validation.constraints.NotBlank;

public record MelhorarSubmancheteRequest(
        String submancheteAtual,
        @NotBlank String conteudo
) {}
