package com.noticiapopular.reacoes.application.dtos;

import com.noticiapopular.reacoes.domain.valueobjects.AlvoTipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ToggleReacaoRequest(
        @NotNull AlvoTipo alvoTipo,
        @NotBlank String alvoId
) {}
