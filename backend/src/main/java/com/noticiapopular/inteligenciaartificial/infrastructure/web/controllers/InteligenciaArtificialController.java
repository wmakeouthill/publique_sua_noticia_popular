package com.noticiapopular.inteligenciaartificial.infrastructure.web.controllers;

import com.noticiapopular.inteligenciaartificial.application.dtos.*;
import com.noticiapopular.inteligenciaartificial.application.usecases.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ia")
@RequiredArgsConstructor
public class InteligenciaArtificialController {

    private final GerarImagemComGeminiUseCase gerarImagem;
    private final GerarTextoNoticiaUseCase gerarTexto;
    private final RefinarTextoNoticiaUseCase refinarTexto;

    @PostMapping("/gerar-imagem")
    public ResponseEntity<ImagemGeradaResponse> gerarImagem(
            @Valid @RequestBody GerarImagemRequest request) {
        return ResponseEntity.ok(gerarImagem.executar(request));
    }

    @PostMapping("/gerar-texto")
    public ResponseEntity<TextoGeradoResponse> gerarTexto(
            @Valid @RequestBody GerarTextoRequest request) {
        return ResponseEntity.ok(gerarTexto.executar(request));
    }

    @PostMapping("/refinar-texto")
    public ResponseEntity<TextoGeradoResponse> refinarTexto(
            @Valid @RequestBody RefinarTextoRequest request) {
        return ResponseEntity.ok(refinarTexto.executar(request));
    }
}
