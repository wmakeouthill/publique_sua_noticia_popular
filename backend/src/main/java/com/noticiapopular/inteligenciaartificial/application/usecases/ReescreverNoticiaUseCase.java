package com.noticiapopular.inteligenciaartificial.application.usecases;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noticiapopular.inteligenciaartificial.application.dtos.ReescreverNoticiaRequest;
import com.noticiapopular.inteligenciaartificial.application.dtos.TextoGeradoResponse;
import com.noticiapopular.inteligenciaartificial.application.ports.out.GeminiApiPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReescreverNoticiaUseCase {

    private final GeminiApiPort geminiApi;
    private final ObjectMapper objectMapper;

    public TextoGeradoResponse executar(ReescreverNoticiaRequest request) {
        String prompt = """
                Você é um editor-chefe de um portal de notícias online. Reescreva o artigo abaixo \
                como uma notícia jornalística profissional, clara e impactante.

                TÍTULO ATUAL: %s

                CONTEÚDO ATUAL:
                %s

                Retorne SOMENTE um JSON válido, sem markdown, no formato abaixo:
                {
                  "titulo": "manchete jornalística impactante, máximo 100 caracteres",
                  "resumo": "lead da notícia com as informações principais, máximo 200 caracteres",
                  "paragrafos": [
                    "primeiro parágrafo",
                    "segundo parágrafo",
                    "..."
                  ]
                }

                Regras:
                - Mantenha os fatos e informações do texto original
                - Use linguagem objetiva, direta e jornalística
                - Estruture com pirâmide invertida (mais importante primeiro)
                - Mínimo de 3 parágrafos, máximo de 8
                - Texto em português brasileiro
                """.formatted(request.titulo(), request.conteudo());

        String resposta = geminiApi.gerarTexto(prompt);
        return parseResposta(resposta);
    }

    private TextoGeradoResponse parseResposta(String resposta) {
        try {
            String json = resposta
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("```", "")
                    .trim();

            var node = objectMapper.readTree(json);
            String titulo = node.path("titulo").asText("");
            String resumo = node.path("resumo").asText("");

            StringBuilder conteudo = new StringBuilder();
            var paragrafos = node.path("paragrafos");
            if (paragrafos.isArray()) {
                for (var p : paragrafos) {
                    if (conteudo.length() > 0) conteudo.append("\n\n");
                    conteudo.append(p.asText(""));
                }
            } else {
                conteudo.append(node.path("conteudo").asText(resposta));
            }

            return new TextoGeradoResponse(titulo, conteudo.toString(), resumo);
        } catch (Exception ex) {
            log.warn("Falha ao parsear resposta da IA, retornando bruto: {}", ex.getMessage());
            return new TextoGeradoResponse("Notícia reescrita", resposta, "");
        }
    }
}
