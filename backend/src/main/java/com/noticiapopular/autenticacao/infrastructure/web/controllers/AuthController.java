package com.noticiapopular.autenticacao.infrastructure.web.controllers;

import com.noticiapopular.autenticacao.application.dtos.*;
import com.noticiapopular.autenticacao.application.usecases.AutenticarComGoogleUseCase;
import com.noticiapopular.autenticacao.application.usecases.BuscarPerfilUseCase;
import com.noticiapopular.autenticacao.application.usecases.RefrescarTokenUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AutenticarComGoogleUseCase autenticarComGoogle;
    private final BuscarPerfilUseCase buscarPerfil;
    private final RefrescarTokenUseCase refrescarToken;

    @PostMapping("/google")
    public ResponseEntity<TokenResponse> loginGoogle(
            @Valid @RequestBody LoginGoogleRequest request) {
        return ResponseEntity.ok(autenticarComGoogle.executar(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(refrescarToken.executar(request));
    }

    @GetMapping("/perfil")
    public ResponseEntity<PerfilUsuarioDTO> perfil(
            @AuthenticationPrincipal String usuarioId) {
        return ResponseEntity.ok(buscarPerfil.executar(usuarioId));
    }
}
