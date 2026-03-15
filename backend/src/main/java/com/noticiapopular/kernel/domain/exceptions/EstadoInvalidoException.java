package com.noticiapopular.kernel.domain.exceptions;

public class EstadoInvalidoException extends RegraDeNegocioException {

    public EstadoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
