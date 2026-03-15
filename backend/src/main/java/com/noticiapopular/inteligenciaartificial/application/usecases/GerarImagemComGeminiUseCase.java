package com.noticiapopular.inteligenciaartificial.application.usecases;

import com.noticiapopular.inteligenciaartificial.application.dtos.GerarImagemRequest;
import com.noticiapopular.inteligenciaartificial.application.dtos.ImagemGeradaResponse;
import com.noticiapopular.inteligenciaartificial.application.ports.out.GeminiApiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GerarImagemComGeminiUseCase {

    private final GeminiApiPort geminiApi;

    public ImagemGeradaResponse executar(GerarImagemRequest request) {
        String imageUrl = geminiApi.gerarImagem(request.prompt());
        return new ImagemGeradaResponse(imageUrl, request.prompt());
    }
}
