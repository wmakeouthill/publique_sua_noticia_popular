package com.noticiapopular.categorias.domain.exceptions;

import com.noticiapopular.kernel.domain.exceptions.EntidadeNaoEncontradaException;

public class CategoriaNaoEncontradaException extends EntidadeNaoEncontradaException {

    public CategoriaNaoEncontradaException(String identificador) {
        super("Categoria", identificador);
    }
}
