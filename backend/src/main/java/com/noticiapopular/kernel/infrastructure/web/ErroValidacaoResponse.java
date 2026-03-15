package com.noticiapopular.kernel.infrastructure.web;

import java.time.Instant;
import java.util.List;

public record ErroValidacaoResponse(
        String mensagem,
        List<CampoErro> campos,
        Instant momento
) {
    public record CampoErro(String campo, String mensagem) {}

    public static ErroValidacaoResponse de(String mensagem, List<CampoErro> campos) {
        return new ErroValidacaoResponse(mensagem, campos, Instant.now());
    }
}
