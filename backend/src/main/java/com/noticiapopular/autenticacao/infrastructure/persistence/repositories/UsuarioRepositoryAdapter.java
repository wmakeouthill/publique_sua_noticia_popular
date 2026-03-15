package com.noticiapopular.autenticacao.infrastructure.persistence.repositories;

import com.noticiapopular.autenticacao.application.ports.out.UsuarioRepositoryPort;
import com.noticiapopular.autenticacao.domain.entities.Usuario;
import com.noticiapopular.autenticacao.infrastructure.persistence.mappers.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository jpaRepository;
    private final UsuarioMapper usuarioMapper;

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        return jpaRepository.findById(id).map(usuarioMapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return jpaRepository.findByEmail(email).map(usuarioMapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorProviderId(String provider, String providerId) {
        return jpaRepository.findByProviderAndProviderId(provider, providerId)
                .map(usuarioMapper::toDomain);
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        var entity = usuarioMapper.toEntity(usuario);
        var salvo = jpaRepository.save(entity);
        return usuarioMapper.toDomain(salvo);
    }

    @Override
    public List<Usuario> listarTodos() {
        return jpaRepository.findAll().stream()
                .map(usuarioMapper::toDomain)
                .toList();
    }
}
