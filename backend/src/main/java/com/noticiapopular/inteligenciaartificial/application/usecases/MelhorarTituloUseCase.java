package com.noticiapopular.inteligenciaartificial.application.usecases;

import com.noticiapopular.inteligenciaartificial.application.dtos.MelhorarTituloRequest;
import com.noticiapopular.inteligenciaartificial.application.dtos.TextoGeradoResponse;
import com.noticiapopular.inteligenciaartificial.application.ports.out.GeminiApiPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MelhorarTituloUseCase {

    private final GeminiApiPort geminiApi;

    public TextoGeradoResponse executar(MelhorarTituloRequest request) {
        String contexto = request.conteudoResumo() != null && !request.conteudoResumo().isBlank()
                ? "\nCONTEXTO DA NOTÍCIA: " + request.conteudoResumo().substring(0, Math.min(300, request.conteudoResumo().length()))
                : "";

        String prompt = """
                Você é um editor de manchetes jornalísticas. Melhore o título abaixo para que seja \
                mais impactante, claro e profissional, adequado para um portal de notícias online.

                TÍTULO ATUAL: %s%s

                Retorne APENAS o novo título melhorado, sem aspas, sem explicações, sem pontuação \
                final desnecessária. Máximo 100 caracteres. Em português brasileiro.
                """.formatted(request.tituloAtual(), contexto);

        String tituloMelhorado = geminiApi.gerarTexto(prompt)
                .replaceAll("```", "")
                .replaceAll("^\"|\"$", "")
                .trim();

        return new TextoGeradoResponse(tituloMelhorado, null, null);
    }
}
