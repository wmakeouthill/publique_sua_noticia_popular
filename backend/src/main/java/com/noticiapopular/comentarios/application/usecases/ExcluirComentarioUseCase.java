package com.noticiapopular.comentarios.application.usecases;

import com.noticiapopular.comentarios.application.ports.out.ComentarioRepositoryPort;
import com.noticiapopular.comentarios.domain.exceptions.ComentarioNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExcluirComentarioUseCase {

    private final ComentarioRepositoryPort comentarioRepository;

    @Transactional
    public void executar(String comentarioId, String usuarioId) {
        var comentario = comentarioRepository.buscarPorId(comentarioId)
                .orElseThrow(() -> new ComentarioNaoEncontradoException(comentarioId));

        boolean ehAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        comentario.validarPermissaoExclusao(usuarioId, ehAdmin);
        comentarioRepository.excluir(comentarioId);
    }
}
