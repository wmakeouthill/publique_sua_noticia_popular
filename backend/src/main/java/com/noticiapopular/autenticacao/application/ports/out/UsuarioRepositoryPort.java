package com.noticiapopular.autenticacao.application.ports.out;

import com.noticiapopular.autenticacao.domain.entities.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositoryPort {

    Optional<Usuario> buscarPorId(String id);

    Optional<Usuario> buscarPorEmail(String email);

    Optional<Usuario> buscarPorProviderId(String provider, String providerId);

    Usuario salvar(Usuario usuario);

    List<Usuario> listarTodos();
}
