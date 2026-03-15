package com.noticiapopular.inteligenciaartificial.application.dtos;

public record TextoGeradoResponse(
        String titulo,
        String conteudo,
        String resumo
) {}
