package com.noticiapopular.autenticacao.application.dtos;

import jakarta.validation.constraints.NotBlank;

public record LoginGoogleRequest(
        @NotBlank(message = "Token Google é obrigatório")
        String googleToken
) {}
