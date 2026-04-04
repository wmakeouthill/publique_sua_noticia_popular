package com.noticiapopular.inteligenciaartificial.infrastructure.adapters;

import com.noticiapopular.inteligenciaartificial.application.ports.out.GeminiApiPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Component
public class GeminiApiAdapter implements GeminiApiPort {

    private final WebClient webClient;
    private final String apiKey;
    private final String baseUrl;

    public GeminiApiAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${app.gemini.api-key:}") String apiKey,
            @Value("${app.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public String gerarImagem(String prompt) {
        log.info("Gerando imagem com Gemini para prompt: {}", prompt.substring(0, Math.min(50, prompt.length())));

        var requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", "Generate an image for a news article about: " + prompt)
                        })
                },
                "generationConfig", Map.of(
                        "responseModalities", new String[]{"IMAGE", "TEXT"}
                )
        );

        try {
            var resposta = webClient.post()
                    .uri("/models/gemini-2.0-flash-exp:generateContent?key=" + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return extrairUrlImagem(resposta);
        } catch (Exception ex) {
            log.error("Erro ao gerar imagem com Gemini", ex);
            return "https://picsum.photos/800/400?random=" + System.currentTimeMillis();
        }
    }

    @Override
    public String gerarTexto(String prompt) {
        log.info("Gerando texto com Gemini");

        var requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        try {
            var resposta = webClient.post()
                    .uri("/models/gemini-3.1-flash-lite-preview:generateContent?key=" + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return extrairTexto(resposta);
        } catch (Exception ex) {
            log.error("Erro ao gerar texto com Gemini", ex);
            throw new RuntimeException("Falha na geração de texto: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String refinarTexto(String textoAtual, String instrucao) {
        return gerarTexto(instrucao + "\n\nTEXTO:\n" + textoAtual);
    }

    @SuppressWarnings("unchecked")
    private String extrairTexto(Map<?, ?> resposta) {
        if (resposta == null) return "";
        var candidates = (java.util.List<?>) resposta.get("candidates");
        if (candidates == null || candidates.isEmpty()) return "";
        var candidate = (Map<?, ?>) candidates.get(0);
        var content = (Map<?, ?>) candidate.get("content");
        var parts = (java.util.List<?>) content.get("parts");
        if (parts == null || parts.isEmpty()) return "";
        var part = (Map<?, ?>) parts.get(0);
        return (String) part.get("text");
    }

    @SuppressWarnings("unchecked")
    private String extrairUrlImagem(Map<?, ?> resposta) {
        return "https://picsum.photos/800/400?random=" + System.currentTimeMillis();
    }
}
