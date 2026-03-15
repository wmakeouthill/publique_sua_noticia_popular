package com.noticiapopular.autenticacao.infrastructure.persistence.mappers;

import com.noticiapopular.autenticacao.domain.entities.Usuario;
import com.noticiapopular.autenticacao.infrastructure.persistence.entities.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioEntity toEntity(Usuario usuario) {
        return UsuarioEntity.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .nome(usuario.getNome())
                .avatarUrl(usuario.getAvatarUrl())
                .provider(usuario.getProvider())
                .providerId(usuario.getProviderId())
                .papel(usuario.getPapel())
                .ativo(usuario.isAtivo())
                .criadoEm(usuario.getCriadoEm())
                .atualizadoEm(usuario.getAtualizadoEm())
                .build();
    }

    public Usuario toDomain(UsuarioEntity entity) {
        return Usuario.reconstituir(
                entity.getId(),
                entity.getEmail(),
                entity.getNome(),
                entity.getAvatarUrl(),
                entity.getProvider(),
                entity.getProviderId(),
                entity.getPapel(),
                entity.isAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
