package com.noticiapopular.noticias.domain.exceptions;

import com.noticiapopular.kernel.domain.exceptions.EntidadeNaoEncontradaException;

public class NoticiaNaoEncontradaException extends EntidadeNaoEncontradaException {

    public NoticiaNaoEncontradaException(String id) {
        super("Notícia", id);
    }
}
