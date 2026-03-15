package com.noticiapopular.kernel.domain.exceptions;

public abstract class DominioException extends RuntimeException {

    protected DominioException(String mensagem) {
        super(mensagem);
    }

    protected DominioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
