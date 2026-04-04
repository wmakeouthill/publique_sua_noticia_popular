package com.noticiapopular.noticias.application.usecases;

import com.noticiapopular.autenticacao.application.ports.out.UsuarioRepositoryPort;
import com.noticiapopular.autenticacao.domain.entities.Usuario;
import com.noticiapopular.noticias.application.dtos.NoticiaDTO;
import com.noticiapopular.noticias.application.ports.out.NoticiaRepositoryPort;
import com.noticiapopular.noticias.domain.entities.Noticia;
import com.noticiapopular.noticias.domain.exceptions.NoticiaNaoEncontradaException;
import com.noticiapopular.reacoes.application.ports.out.ReacaoRepositoryPort;
import com.noticiapopular.reacoes.domain.valueobjects.AlvoTipo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuscarNoticiaPorIdUseCase {

    private final NoticiaRepositoryPort noticiaRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final ReacaoRepositoryPort reacaoRepository;

    @Transactional
    public NoticiaDTO executar(String id, String usuarioId) {
        Noticia noticia = noticiaRepository.buscarPorId(id)
                .orElseThrow(() -> new NoticiaNaoEncontradaException(id));

        noticia.incrementarVisualizacao();
        noticiaRepository.salvar(noticia);

        Usuario autor = usuarioRepository.buscarPorId(noticia.getAutorId()).orElse(null);
        String nome = autor != null ? autor.getNome() : null;
        String avatar = autor != null ? autor.getAvatarUrl() : null;

        long totalLikes = reacaoRepository.contarPorAlvo(AlvoTipo.NOTICIA, id);
        boolean likedByMe = reacaoRepository.existeLike(usuarioId != null ? usuarioId : "", AlvoTipo.NOTICIA, id);

        return NoticiaDTO.from(noticia, nome, avatar, totalLikes, likedByMe);
    }
}
