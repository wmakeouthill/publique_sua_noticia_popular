package com.noticiapopular.noticias.application.usecases;

import com.noticiapopular.autenticacao.application.ports.out.UsuarioRepositoryPort;
import com.noticiapopular.autenticacao.domain.exceptions.UsuarioNaoEncontradoException;
import com.noticiapopular.noticias.application.dtos.EditarNoticiaRequest;
import com.noticiapopular.noticias.application.dtos.NoticiaDTO;
import com.noticiapopular.noticias.application.ports.out.NoticiaRepositoryPort;
import com.noticiapopular.noticias.domain.entities.Noticia;
import com.noticiapopular.noticias.domain.exceptions.NoticiaNaoEncontradaException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EditarNoticiaUseCase {

    private final NoticiaRepositoryPort noticiaRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    @Transactional
    public NoticiaDTO executar(String noticiaId, EditarNoticiaRequest request, String usuarioId) {
        Noticia noticia = noticiaRepository.buscarPorId(noticiaId)
                .orElseThrow(() -> new NoticiaNaoEncontradaException(noticiaId));

        boolean ehAdmin = usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId))
                .ehAdmin();

        noticia.validarPermissaoEdicao(usuarioId, ehAdmin);
        noticia.editar(request.titulo(), request.conteudo(), request.resumo(), request.imagemUrl());

        return NoticiaDTO.from(noticiaRepository.salvar(noticia), null, null, 0L, false);
    }
}
