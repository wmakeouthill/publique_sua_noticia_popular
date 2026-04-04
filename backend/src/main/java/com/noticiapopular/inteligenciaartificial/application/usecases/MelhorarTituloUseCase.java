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
                Você é um editor de manchetes jornalísticas. Reescreva a manchete abaixo de forma \
                mais clara e direta, usando SOMENTE as informações presentes no título e no contexto \
                fornecidos. Não invente fatos, nomes, números ou detalhes que não estejam no texto.

                MANCHETE ATUAL: %s%s

                Regras obrigatórias:
                - Use apenas o que está no contexto acima
                - Não acrescente informações novas
                - Seja direto e jornalístico
                - Máximo 100 caracteres
                - Sem aspas, sem explicações, em português brasileiro
                """.formatted(request.tituloAtual(), contexto);

        String tituloMelhorado = geminiApi.gerarTexto(prompt)
                .replaceAll("```", "")
                .replaceAll("^\"|\"$", "")
                .trim();

        return new TextoGeradoResponse(tituloMelhorado, null, null);
    }
}
