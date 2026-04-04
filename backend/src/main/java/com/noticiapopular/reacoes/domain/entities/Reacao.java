package com.noticiapopular.reacoes.domain.entities;

import com.noticiapopular.reacoes.domain.valueobjects.AlvoTipo;

import java.time.Instant;
import java.util.UUID;

public class Reacao {

    private final String id;
    private final String usuarioId;
    private final AlvoTipo alvoTipo;
    private final String alvoId;
    private final Instant criadoEm;

    private Reacao(String id, String usuarioId, AlvoTipo alvoTipo, String alvoId, Instant criadoEm) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.alvoTipo = alvoTipo;
        this.alvoId = alvoId;
        this.criadoEm = criadoEm;
    }

    public static Reacao criar(String usuarioId, AlvoTipo alvoTipo, String alvoId) {
        return new Reacao(UUID.randomUUID().toString(), usuarioId, alvoTipo, alvoId, Instant.now());
    }

    public static Reacao reconstituir(String id, String usuarioId, AlvoTipo alvoTipo,
                                      String alvoId, Instant criadoEm) {
        return new Reacao(id, usuarioId, alvoTipo, alvoId, criadoEm);
    }

    public String getId() { return id; }
    public String getUsuarioId() { return usuarioId; }
    public AlvoTipo getAlvoTipo() { return alvoTipo; }
    public String getAlvoId() { return alvoId; }
    public Instant getCriadoEm() { return criadoEm; }
}
