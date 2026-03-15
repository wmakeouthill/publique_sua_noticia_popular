package com.noticiapopular.inteligenciaartificial.application.ports.out;

public interface GeminiApiPort {

    String gerarImagem(String prompt);

    String gerarTexto(String prompt);

    String refinarTexto(String textoAtual, String instrucao);
}
