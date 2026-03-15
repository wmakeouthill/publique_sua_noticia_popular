package com.noticiapopular.autenticacao.domain.valueobjects;

import com.noticiapopular.kernel.domain.exceptions.ValidacaoException;

import java.util.regex.Pattern;

public record Email(String valor) {

    private static final Pattern PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public Email {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoException("Email é obrigatório");
        }
        valor = valor.toLowerCase().trim();
        if (!PATTERN.matcher(valor).matches()) {
            throw new ValidacaoException("Email inválido: " + valor);
        }
    }

    public static Email of(String valor) {
        return new Email(valor);
    }
}
