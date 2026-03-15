package com.noticiapopular.categorias.application.dtos;

import jakarta.validation.constraints.Size;

public record AtualizarCategoriaRequest(
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
        String descricao,

        @Size(max = 10, message = "Ícone deve ter no máximo 10 caracteres")
        String icone,

        Integer ordem
) {}
