package com.noticiapopular.comentarios.domain.exceptions;

import com.noticiapopular.kernel.domain.exceptions.EntidadeNaoEncontradaException;

public class ComentarioNaoEncontradoException extends EntidadeNaoEncontradaException {
    public ComentarioNaoEncontradoException(String id) {
        super("Comentário", id);
    }
}
