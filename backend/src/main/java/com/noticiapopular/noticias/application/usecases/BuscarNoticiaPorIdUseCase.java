package com.noticiapopular.noticias.application.usecases;

import com.noticiapopular.autenticacao.application.ports.out.UsuarioRepositoryPort;
import com.noticiapopular.autenticacao.domain.entities.Usuario;
import com.noticiapopular.noticias.application.dtos.NoticiaDTO;
import com.noticiapopular.noticias.application.ports.out.NoticiaRepositoryPort;
import com.noticiapopular.noticias.domain.entities.Noticia;
import com.noticiapopular.noticias.domain.exceptions.NoticiaNaoEncontradaException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuscarNoticiaPorIdUseCase {

    private final NoticiaRepositoryPort noticiaRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    @Transactional
    public NoticiaDTO executar(String id) {
        Noticia noticia = noticiaRepository.buscarPorId(id)
                .orElseThrow(() -> new NoticiaNaoEncontradaException(id));

        noticia.incrementarVisualizacao();
        noticiaRepository.salvar(noticia);

        Usuario autor = usuarioRepository.buscarPorId(noticia.getAutorId()).orElse(null);
        String nome = autor != null ? autor.getNome() : null;
        String avatar = autor != null ? autor.getAvatarUrl() : null;

        return NoticiaDTO.from(noticia, nome, avatar);
    }
}
