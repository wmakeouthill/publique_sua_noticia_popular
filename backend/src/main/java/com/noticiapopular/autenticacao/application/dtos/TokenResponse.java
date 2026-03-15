package com.noticiapopular.autenticacao.application.dtos;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        boolean admin,
        long expiresIn
) {}
