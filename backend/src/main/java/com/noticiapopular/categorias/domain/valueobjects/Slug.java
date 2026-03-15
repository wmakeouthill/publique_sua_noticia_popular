package com.noticiapopular.categorias.domain.valueobjects;

import com.noticiapopular.kernel.domain.exceptions.ValidacaoException;

import java.text.Normalizer;

public record Slug(String valor) {

    public Slug {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoException("Slug não pode ser vazio");
        }
    }

    public static Slug from(String texto) {
        String slug = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return new Slug(slug);
    }
}
