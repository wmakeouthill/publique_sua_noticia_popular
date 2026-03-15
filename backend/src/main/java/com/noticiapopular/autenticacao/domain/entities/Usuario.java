package com.noticiapopular.autenticacao.domain.entities;

import com.noticiapopular.autenticacao.domain.valueobjects.Email;
import com.noticiapopular.autenticacao.domain.valueobjects.PapelUsuario;
import com.noticiapopular.kernel.domain.exceptions.ValidacaoException;

import java.time.Instant;
import java.util.UUID;

public class Usuario {

    private final String id;
    private final Email email;
    private String nome;
    private String avatarUrl;
    private final String provider;
    private final String providerId;
    private PapelUsuario papel;
    private boolean ativo;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private Usuario(String id, Email email, String nome, String avatarUrl,
                    String provider, String providerId, PapelUsuario papel,
                    boolean ativo, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.email = email;
        this.nome = nome;
        this.avatarUrl = avatarUrl;
        this.provider = provider;
        this.providerId = providerId;
        this.papel = papel;
        this.ativo = ativo;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static Usuario criarComGoogle(String email, String nome,
                                          String avatarUrl, String providerId) {
        validarCamposObrigatorios(nome, providerId);
        return new Usuario(
                UUID.randomUUID().toString(),
                Email.of(email),
                nome.trim(),
                avatarUrl,
                "google",
                providerId,
                PapelUsuario.USUARIO,
                true,
                Instant.now(),
                null
        );
    }

    public static Usuario reconstituir(String id, String email, String nome,
                                        String avatarUrl, String provider,
                                        String providerId, PapelUsuario papel,
                                        boolean ativo, Instant criadoEm,
                                        Instant atualizadoEm) {
        return new Usuario(id, Email.of(email), nome, avatarUrl, provider,
                providerId, papel, ativo, criadoEm, atualizadoEm);
    }

    public void ativar() {
        this.ativo = true;
        this.atualizadoEm = Instant.now();
    }

    public void desativar() {
        this.ativo = false;
        this.atualizadoEm = Instant.now();
    }

    public boolean ehAdmin() {
        return this.papel == PapelUsuario.ADMIN;
    }

    public void promoverParaAdmin() {
        this.papel = PapelUsuario.ADMIN;
        this.atualizadoEm = Instant.now();
    }

    public void atualizarPerfil(String novoNome, String novoAvatarUrl) {
        if (novoNome != null && !novoNome.isBlank()) {
            this.nome = novoNome.trim();
        }
        if (novoAvatarUrl != null) {
            this.avatarUrl = novoAvatarUrl;
        }
        this.atualizadoEm = Instant.now();
    }

    private static void validarCamposObrigatorios(String nome, String providerId) {
        if (nome == null || nome.isBlank()) {
            throw new ValidacaoException("Nome do usuário é obrigatório");
        }
        if (providerId == null || providerId.isBlank()) {
            throw new ValidacaoException("Provider ID é obrigatório");
        }
    }

    public String getId() { return id; }
    public String getEmail() { return email.valor(); }
    public String getNome() { return nome; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getProvider() { return provider; }
    public String getProviderId() { return providerId; }
    public PapelUsuario getPapel() { return papel; }
    public boolean isAtivo() { return ativo; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
