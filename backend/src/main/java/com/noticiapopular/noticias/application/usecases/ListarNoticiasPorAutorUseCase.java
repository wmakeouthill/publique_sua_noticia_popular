package com.noticiapopular.noticias.application.usecases;

import com.noticiapopular.autenticacao.application.ports.out.UsuarioRepositoryPort;
import com.noticiapopular.autenticacao.domain.entities.Usuario;
import com.noticiapopular.noticias.application.dtos.NoticiaResumoDTO;
import com.noticiapopular.noticias.application.ports.out.NoticiaRepositoryPort;
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
public class ListarNoticiasPorAutorUseCase {

    private final NoticiaRepositoryPort noticiaRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final ReacaoRepositoryPort reacaoRepository;

    @Transactional(readOnly = true)
    public Page<NoticiaResumoDTO> executar(String autorId, Pageable pageable, String usuarioId) {
        Usuario autor = usuarioRepository.buscarPorId(autorId).orElse(null);
        String nome = autor != null ? autor.getNome() : null;
        String avatar = autor != null ? autor.getAvatarUrl() : null;

        Page<com.noticiapopular.noticias.domain.entities.Noticia> pagina =
                noticiaRepository.listarPorAutor(autorId, pageable);

        List<String> ids = pagina.getContent().stream().map(n -> n.getId()).toList();
        Map<String, Long> likeCounts = reacaoRepository.contarPorAlvos(AlvoTipo.NOTICIA, ids);
        Set<String> likedByMe = reacaoRepository.filtrarLikedByUsuario(usuarioId, AlvoTipo.NOTICIA, ids);

        return pagina.map(n -> {
            long total = likeCounts.getOrDefault(n.getId(), 0L);
            boolean liked = likedByMe.contains(n.getId());
            return NoticiaResumoDTO.from(n, nome, avatar, total, liked);
        });
    }
}
