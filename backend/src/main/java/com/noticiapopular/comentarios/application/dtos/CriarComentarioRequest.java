package com.noticiapopular.comentarios.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarComentarioRequest(
        @NotBlank(message = "Comentário não pode ser vazio")
        @Size(max = 1000, message = "Comentário deve ter no máximo 1000 caracteres")
        String conteudo
) {}
