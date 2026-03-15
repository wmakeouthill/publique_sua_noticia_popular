package com.noticiapopular.inteligenciaartificial.application.usecases;

import com.noticiapopular.inteligenciaartificial.application.dtos.RefinarTextoRequest;
import com.noticiapopular.inteligenciaartificial.application.dtos.TextoGeradoResponse;
import com.noticiapopular.inteligenciaartificial.application.ports.out.GeminiApiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefinarTextoNoticiaUseCase {

    private final GeminiApiPort geminiApi;

    public TextoGeradoResponse executar(RefinarTextoRequest request) {
        String prompt = """
                Você é um editor jornalístico. Refine o texto abaixo seguindo a instrução dada.
                
                INSTRUÇÃO: %s
                
                TEXTO ATUAL:
                %s
                
                Retorne apenas o texto refinado, sem explicações adicionais.
                """.formatted(request.instrucao(), request.textoAtual());

        String textoRefinado = geminiApi.gerarTexto(prompt);
        return new TextoGeradoResponse(null, textoRefinado, null);
    }
}
