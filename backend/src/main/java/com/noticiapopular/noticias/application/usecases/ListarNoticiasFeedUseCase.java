package com.noticiapopular.noticias.application.usecases;

import com.noticiapopular.autenticacao.application.ports.out.UsuarioRepositoryPort;
import com.noticiapopular.autenticacao.domain.entities.Usuario;
import com.noticiapopular.noticias.application.dtos.FiltroNoticiaRequest;
import com.noticiapopular.noticias.application.dtos.NoticiaResumoDTO;
import com.noticiapopular.noticias.application.ports.out.NoticiaRepositoryPort;
import com.noticiapopular.noticias.domain.entities.Noticia;
import com.noticiapopular.reacoes.application.ports.out.ReacaoRepositoryPort;
import com.noticiapopular.reacoes.domain.valueobjects.AlvoTipo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ListarNoticiasFeedUseCase {

    private final NoticiaRepositoryPort noticiaRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final ReacaoRepositoryPort reacaoRepository;

    @Transactional(readOnly = true)
    public Page<NoticiaResumoDTO> executar(FiltroNoticiaRequest filtro, Pageable pageable,
                                            String usuarioId, String ordenacao) {
        Page<Noticia> pagina;
        if ("MAIS_CURTIDO".equals(ordenacao)) {
            pagina = noticiaRepository.listarPublicadasPorLikes(filtro.categoriaId(), filtro.busca(), pageable);
        } else {
            pagina = noticiaRepository.listarPublicadas(filtro.categoriaId(), filtro.busca(), pageable);
        }

        List<String> ids = pagina.getContent().stream().map(Noticia::getId).toList();
        Map<String, Long> likeCounts = reacaoRepository.contarPorAlvos(AlvoTipo.NOTICIA, ids);
        Set<String> likedByMe = reacaoRepository.filtrarLikedByUsuario(usuarioId, AlvoTipo.NOTICIA, ids);

        return pagina.map(noticia -> {
            Usuario autor = usuarioRepository.buscarPorId(noticia.getAutorId()).orElse(null);
            String nome = autor != null ? autor.getNome() : null;
            String avatar = autor != null ? autor.getAvatarUrl() : null;
            long total = likeCounts.getOrDefault(noticia.getId(), 0L);
            boolean liked = likedByMe.contains(noticia.getId());
            return NoticiaResumoDTO.from(noticia, nome, avatar, total, liked);
        });
    }
}
