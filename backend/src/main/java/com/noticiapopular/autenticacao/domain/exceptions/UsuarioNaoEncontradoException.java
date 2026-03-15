package com.noticiapopular.autenticacao.domain.exceptions;

import com.noticiapopular.kernel.domain.exceptions.EntidadeNaoEncontradaException;

public class UsuarioNaoEncontradoException extends EntidadeNaoEncontradaException {

    public UsuarioNaoEncontradoException(String identificador) {
        super("Usuário", identificador);
    }
}
