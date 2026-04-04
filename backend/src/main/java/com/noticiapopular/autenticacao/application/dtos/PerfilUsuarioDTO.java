package com.noticiapopular.autenticacao.application.dtos;

import com.noticiapopular.autenticacao.domain.entities.Usuario;

import java.time.Instant;

public record PerfilUsuarioDTO(
        String id,
        String email,
        String nome,
        String avatarUrl,
        String papel,
        boolean ativo,
        Instant criadoEm
) {
    public static PerfilUsuarioDTO from(Usuario usuario) {
        return new PerfilUsuarioDTO(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNome(),
                usuario.getAvatarUrl(),
                usuario.getPapel().name(),
                usuario.isAtivo(),
                usuario.getCriadoEm()
        );
    }
}
