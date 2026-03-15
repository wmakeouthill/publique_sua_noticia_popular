package com.noticiapopular.noticias.domain.entities;

import com.noticiapopular.kernel.domain.exceptions.AcessoNegadoException;
import com.noticiapopular.kernel.domain.exceptions.ValidacaoException;
import com.noticiapopular.noticias.domain.valueobjects.ConteudoNoticia;
import com.noticiapopular.noticias.domain.valueobjects.StatusNoticia;

import java.time.Instant;
import java.util.UUID;

public class Noticia {

    private final String id;
    private String titulo;
    private ConteudoNoticia conteudo;
    private String resumo;
    private String imagemUrl;
    private final String categoriaId;
    private final String autorId;
    private StatusNoticia status;
    private long visualizacoes;
    private final Instant criadoEm;
    private Instant atualizadoEm;
    private Instant publicadoEm;

    private Noticia(String id, String titulo, ConteudoNoticia conteudo,
                    String resumo, String imagemUrl, String categoriaId,
                    String autorId, StatusNoticia status, long visualizacoes,
                    Instant criadoEm, Instant atualizadoEm, Instant publicadoEm) {
        this.id = id;
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.resumo = resumo;
        this.imagemUrl = imagemUrl;
        this.categoriaId = categoriaId;
        this.autorId = autorId;
        this.status = status;
        this.visualizacoes = visualizacoes;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
        this.publicadoEm = publicadoEm;
    }

    public static Noticia criar(String titulo, String conteudo, String resumo,
                                 String imagemUrl, String categoriaId, String autorId) {
        validarTitulo(titulo);
        validarConteudo(conteudo);
        return new Noticia(
                UUID.randomUUID().toString(),
                titulo.trim(),
                ConteudoNoticia.of(conteudo),
                resumo,
                imagemUrl,
                categoriaId,
                autorId,
                StatusNoticia.RASCUNHO,
                0L,
                Instant.now(),
                null,
                null
        );
    }

    public static Noticia reconstituir(String id, String titulo, String conteudo,
                                        String resumo, String imagemUrl,
                                        String categoriaId, String autorId,
                                        StatusNoticia status, long visualizacoes,
                                        Instant criadoEm, Instant atualizadoEm,
                                        Instant publicadoEm) {
        return new Noticia(id, titulo, ConteudoNoticia.of(conteudo), resumo,
                imagemUrl, categoriaId, autorId, status, visualizacoes,
                criadoEm, atualizadoEm, publicadoEm);
    }

    public void publicar() {
        if (this.status == StatusNoticia.PUBLICADA) {
            return;
        }
        this.status = StatusNoticia.PUBLICADA;
        this.publicadoEm = Instant.now();
        this.atualizadoEm = Instant.now();
    }

    public void arquivar() {
        this.status = StatusNoticia.ARQUIVADA;
        this.atualizadoEm = Instant.now();
    }

    public void editar(String titulo, String conteudo, String resumo, String imagemUrl) {
        if (titulo != null && !titulo.isBlank()) {
            validarTitulo(titulo);
            this.titulo = titulo.trim();
        }
        if (conteudo != null && !conteudo.isBlank()) {
            validarConteudo(conteudo);
            this.conteudo = ConteudoNoticia.of(conteudo);
        }
        if (resumo != null) {
            this.resumo = resumo;
        }
        if (imagemUrl != null) {
            this.imagemUrl = imagemUrl;
        }
        this.atualizadoEm = Instant.now();
    }

    public void incrementarVisualizacao() {
        this.visualizacoes++;
    }

    public boolean pertenceAoAutor(String usuarioId) {
        return this.autorId.equals(usuarioId);
    }

    public void validarPermissaoEdicao(String usuarioId, boolean ehAdmin) {
        if (!pertenceAoAutor(usuarioId) && !ehAdmin) {
            throw new AcessoNegadoException("Apenas o autor ou admin pode editar esta notícia");
        }
    }

    public void validarPermissaoExclusao(String usuarioId, boolean ehAdmin) {
        if (!pertenceAoAutor(usuarioId) && !ehAdmin) {
            throw new AcessoNegadoException("Apenas o autor ou admin pode excluir esta notícia");
        }
    }

    private static void validarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new ValidacaoException("Título é obrigatório");
        }
        if (titulo.length() > 255) {
            throw new ValidacaoException("Título deve ter no máximo 255 caracteres");
        }
    }

    private static void validarConteudo(String conteudo) {
        if (conteudo == null || conteudo.isBlank()) {
            throw new ValidacaoException("Conteúdo é obrigatório");
        }
    }

    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getConteudo() { return conteudo.valor(); }
    public String getResumo() { return resumo; }
    public String getImagemUrl() { return imagemUrl; }
    public String getCategoriaId() { return categoriaId; }
    public String getAutorId() { return autorId; }
    public StatusNoticia getStatus() { return status; }
    public long getVisualizacoes() { return visualizacoes; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
    public Instant getPublicadoEm() { return publicadoEm; }
}
