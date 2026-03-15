package com.noticiapopular.inteligenciaartificial.application.usecases;

import com.noticiapopular.inteligenciaartificial.application.dtos.GerarTextoRequest;
import com.noticiapopular.inteligenciaartificial.application.dtos.TextoGeradoResponse;
import com.noticiapopular.inteligenciaartificial.application.ports.out.GeminiApiPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GerarTextoNoticiaUseCase {

    private final GeminiApiPort geminiApi;
    private final ObjectMapper objectMapper;

    public TextoGeradoResponse executar(GerarTextoRequest request) {
        String prompt = construirPrompt(request);
        String resposta = geminiApi.gerarTexto(prompt);
        return parseResposta(resposta);
    }

    private String construirPrompt(GerarTextoRequest request) {
        return """
                Você é um jornalista experiente. Escreva uma notícia completa com base no seguinte contexto:
                
                ASSUNTO: %s
                %s
                
                Retorne um JSON no formato:
                {
                  "titulo": "título da notícia (máximo 100 caracteres)",
                  "resumo": "resumo em até 200 caracteres",
                  "conteudo": "conteúdo completo da notícia em formato JSON de blocos do editor"
                }
                
                O conteúdo deve ser em português brasileiro, objetivo, informativo e jornalístico.
                """.formatted(
                request.prompt(),
                request.categoriaHint() != null ? "CATEGORIA: " + request.categoriaHint() : ""
        );
    }

    private TextoGeradoResponse parseResposta(String resposta) {
        try {
            var json = objectMapper.readTree(resposta
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim());
            return new TextoGeradoResponse(
                    json.path("titulo").asText("Título gerado pela IA"),
                    json.path("conteudo").asText(resposta),
                    json.path("resumo").asText("")
            );
        } catch (Exception ex) {
            log.warn("Falha ao parsear resposta JSON da IA, retornando como texto bruto", ex);
            return new TextoGeradoResponse("Notícia gerada", resposta, "");
        }
    }
}
