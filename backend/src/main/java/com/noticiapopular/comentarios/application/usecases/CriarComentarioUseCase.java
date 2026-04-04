package com.noticiapopular.comentarios.application.usecases;

import com.noticiapopular.autenticacao.application.ports.out.UsuarioRepositoryPort;
import com.noticiapopular.autenticacao.domain.exceptions.UsuarioNaoEncontradoException;
import com.noticiapopular.comentarios.application.dtos.ComentarioDTO;
import com.noticiapopular.comentarios.application.dtos.CriarComentarioRequest;
import com.noticiapopular.comentarios.application.ports.out.ComentarioRepositoryPort;
import com.noticiapopular.comentarios.domain.entities.Comentario;
import com.noticiapopular.noticias.application.ports.out.NoticiaRepositoryPort;
import com.noticiapopular.noticias.domain.exceptions.NoticiaNaoEncontradaException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CriarComentarioUseCase {

    private final ComentarioRepositoryPort comentarioRepository;
    private final NoticiaRepositoryPort noticiaRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    @Transactional
    public ComentarioDTO executar(String noticiaId, CriarComentarioRequest request, String autorId) {
        noticiaRepository.buscarPorId(noticiaId)
                .orElseThrow(() -> new NoticiaNaoEncontradaException(noticiaId));

        var autor = usuarioRepository.buscarPorId(autorId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(autorId));

        var comentario = Comentario.criar(noticiaId, autorId, request.conteudo());
        var salvo = comentarioRepository.salvar(comentario);

        return ComentarioDTO.from(salvo, autor.getNome(), autor.getAvatarUrl());
    }
}
