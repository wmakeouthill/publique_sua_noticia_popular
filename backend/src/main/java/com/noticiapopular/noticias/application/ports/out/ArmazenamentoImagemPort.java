package com.noticiapopular.noticias.application.ports.out;

public interface ArmazenamentoImagemPort {

    String salvar(byte[] dados, String nomeOriginal, String contentType);

    void excluir(String caminhoImagem);

    String obterUrlPublica(String caminhoImagem);
}
