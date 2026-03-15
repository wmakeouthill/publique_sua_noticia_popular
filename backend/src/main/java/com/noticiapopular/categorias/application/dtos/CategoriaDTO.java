package com.noticiapopular.categorias.application.dtos;

import com.noticiapopular.categorias.domain.entities.Categoria;

public record CategoriaDTO(
        String id,
        String nome,
        String slug,
        String descricao,
        String icone,
        boolean ativa,
        int ordem
) {
    public static CategoriaDTO from(Categoria categoria) {
        return new CategoriaDTO(
                categoria.getId(),
                categoria.getNome(),
                categoria.getSlug(),
                categoria.getDescricao(),
                categoria.getIcone(),
                categoria.isAtiva(),
                categoria.getOrdem()
        );
    }
}
