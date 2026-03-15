package com.noticiapopular.noticias.domain.valueobjects;

import com.noticiapopular.kernel.domain.exceptions.ValidacaoException;

public record ConteudoNoticia(String valor) {

    public ConteudoNoticia {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoException("Conteúdo da notícia não pode ser vazio");
        }
    }

    public static ConteudoNoticia of(String valor) {
        return new ConteudoNoticia(valor);
    }
}
