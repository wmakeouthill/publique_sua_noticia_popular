package com.noticiapopular.autenticacao.application.usecases;

import com.noticiapopular.autenticacao.application.dtos.PerfilUsuarioDTO;
import com.noticiapopular.autenticacao.application.ports.out.UsuarioRepositoryPort;
import com.noticiapopular.autenticacao.domain.exceptions.UsuarioNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuscarPerfilUseCase {

    private final UsuarioRepositoryPort usuarioRepository;

    @Transactional(readOnly = true)
    public PerfilUsuarioDTO executar(String usuarioId) {
        return usuarioRepository.buscarPorId(usuarioId)
                .map(PerfilUsuarioDTO::from)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));
    }
}
