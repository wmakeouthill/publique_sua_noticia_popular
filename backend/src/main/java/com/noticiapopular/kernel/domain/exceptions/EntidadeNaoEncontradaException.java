package com.noticiapopular.kernel.domain.exceptions;

public class EntidadeNaoEncontradaException extends DominioException {

    public EntidadeNaoEncontradaException(String entidade, String identificador) {
        super(entidade + " não encontrado(a) com identificador: " + identificador);
    }
}
