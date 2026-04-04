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
    private final ReescreverNoticiaUseCase reescreverNoticia;
    private final MelhorarTituloUseCase melhorarTitulo;
    private final MelhorarSubmancheteUseCase melhorarSubmanchete;

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

    @PostMapping("/reescrever-noticia")
    public ResponseEntity<TextoGeradoResponse> reescreverNoticia(
            @Valid @RequestBody ReescreverNoticiaRequest request) {
        return ResponseEntity.ok(reescreverNoticia.executar(request));
    }

    @PostMapping("/melhorar-titulo")
    public ResponseEntity<TextoGeradoResponse> melhorarTitulo(
            @Valid @RequestBody MelhorarTituloRequest request) {
        return ResponseEntity.ok(melhorarTitulo.executar(request));
    }

    @PostMapping("/melhorar-submanchete")
    public ResponseEntity<TextoGeradoResponse> melhorarSubmanchete(
            @Valid @RequestBody MelhorarSubmancheteRequest request) {
        return ResponseEntity.ok(melhorarSubmanchete.executar(request));
    }
}
