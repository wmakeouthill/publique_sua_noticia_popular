package com.noticiapopular.comentarios.domain.entities;

import com.noticiapopular.kernel.domain.exceptions.AcessoNegadoException;
import com.noticiapopular.kernel.domain.exceptions.ValidacaoException;

import java.time.Instant;
import java.util.UUID;

public class Comentario {

    private final String id;
    private final String noticiaId;
    private final String autorId;
    private String conteudo;
    private final Instant criadoEm;

    private Comentario(String id, String noticiaId, String autorId, String conteudo, Instant criadoEm) {
        this.id = id;
        this.noticiaId = noticiaId;
        this.autorId = autorId;
        this.conteudo = conteudo;
        this.criadoEm = criadoEm;
    }

    public static Comentario criar(String noticiaId, String autorId, String conteudo) {
        validarConteudo(conteudo);
        return new Comentario(
                UUID.randomUUID().toString(),
                noticiaId,
                autorId,
                conteudo.trim(),
                Instant.now()
        );
    }

    public static Comentario reconstituir(String id, String noticiaId, String autorId,
                                          String conteudo, Instant criadoEm) {
        return new Comentario(id, noticiaId, autorId, conteudo, criadoEm);
    }

    public void validarPermissaoExclusao(String usuarioId, boolean ehAdmin) {
        if (!this.autorId.equals(usuarioId) && !ehAdmin) {
            throw new AcessoNegadoException("Apenas o autor ou admin pode excluir este comentário");
        }
    }

    private static void validarConteudo(String conteudo) {
        if (conteudo == null || conteudo.isBlank()) {
            throw new ValidacaoException("Comentário não pode ser vazio");
        }
        if (conteudo.trim().length() > 1000) {
            throw new ValidacaoException("Comentário deve ter no máximo 1000 caracteres");
        }
    }

    public String getId() { return id; }
    public String getNoticiaId() { return noticiaId; }
    public String getAutorId() { return autorId; }
    public String getConteudo() { return conteudo; }
    public Instant getCriadoEm() { return criadoEm; }
}
