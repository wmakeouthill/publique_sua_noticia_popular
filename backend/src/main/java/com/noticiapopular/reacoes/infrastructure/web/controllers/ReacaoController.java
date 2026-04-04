package com.noticiapopular.reacoes.infrastructure.web.controllers;

import com.noticiapopular.reacoes.application.dtos.ReacaoStatusDTO;
import com.noticiapopular.reacoes.application.dtos.ToggleReacaoRequest;
import com.noticiapopular.reacoes.application.usecases.ToggleReacaoUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reacoes")
@RequiredArgsConstructor
public class ReacaoController {

    private final ToggleReacaoUseCase toggleReacao;

    @PostMapping("/toggle")
    public ResponseEntity<ReacaoStatusDTO> toggle(
            @Valid @RequestBody ToggleReacaoRequest request,
            @AuthenticationPrincipal String usuarioId) {
        return ResponseEntity.ok(toggleReacao.executar(usuarioId, request.alvoTipo(), request.alvoId()));
    }
}
