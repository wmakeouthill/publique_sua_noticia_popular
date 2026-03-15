package com.noticiapopular.kernel.infrastructure.web;

import java.time.Instant;

public record ErroResponse(
        String mensagem,
        String codigo,
        Instant momento
) {
    public static ErroResponse de(String mensagem, String codigo) {
        return new ErroResponse(mensagem, codigo, Instant.now());
    }
}
