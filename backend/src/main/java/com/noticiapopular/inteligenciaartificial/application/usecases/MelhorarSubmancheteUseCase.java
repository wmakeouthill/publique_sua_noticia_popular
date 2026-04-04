package com.noticiapopular.inteligenciaartificial.application.usecases;

import com.noticiapopular.inteligenciaartificial.application.dtos.MelhorarSubmancheteRequest;
import com.noticiapopular.inteligenciaartificial.application.dtos.TextoGeradoResponse;
import com.noticiapopular.inteligenciaartificial.application.ports.out.GeminiApiPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MelhorarSubmancheteUseCase {

    private final GeminiApiPort geminiApi;

    public TextoGeradoResponse executar(MelhorarSubmancheteRequest request) {
        String baseAtual = request.submancheteAtual() != null && !request.submancheteAtual().isBlank()
                ? "\nSUBMANCHETE ATUAL (melhore esta): " + request.submancheteAtual()
                : "\n(Não há submanchete ainda. Crie uma com base no conteúdo abaixo.)";

        String conteudoResumido = request.conteudo().substring(0, Math.min(500, request.conteudo().length()));

        String prompt = """
                Você é um editor de um portal de notícias. Escreva uma submanchete jornalística \
                (subtítulo) para a notícia abaixo.%s

                CONTEÚDO DA NOTÍCIA:
                %s

                Regras obrigatórias:
                - Use APENAS os fatos e informações presentes no conteúdo acima
                - Não invente detalhes, contextos ou informações que não estejam no texto
                - A submanchete deve complementar a manchete, expandindo a informação principal
                - Seja direto, objetivo e jornalístico
                - Máximo de 200 caracteres
                - Retorne APENAS o texto da submanchete, sem aspas, sem explicações, em português brasileiro
                """.formatted(baseAtual, conteudoResumido);

        String resultado = geminiApi.gerarTexto(prompt)
                .replaceAll("```", "")
                .replaceAll("^\"|\"$", "")
                .trim();

        return new TextoGeradoResponse(null, resultado, null);
    }
}
