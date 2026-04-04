package com.noticiapopular.comentarios.application.usecases;

import com.noticiapopular.autenticacao.application.ports.out.UsuarioRepositoryPort;
import com.noticiapopular.autenticacao.domain.entities.Usuario;
import com.noticiapopular.comentarios.application.dtos.ComentarioDTO;
import com.noticiapopular.comentarios.application.ports.out.ComentarioRepositoryPort;
import com.noticiapopular.comentarios.domain.entities.Comentario;
import com.noticiapopular.noticias.application.ports.out.NoticiaRepositoryPort;
import com.noticiapopular.noticias.domain.exceptions.NoticiaNaoEncontradaException;
import com.noticiapopular.reacoes.application.ports.out.ReacaoRepositoryPort;
import com.noticiapopular.reacoes.domain.valueobjects.AlvoTipo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListarComentariosUseCase {

    private final ComentarioRepositoryPort comentarioRepository;
    private final NoticiaRepositoryPort noticiaRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final ReacaoRepositoryPort reacaoRepository;

    @Transactional(readOnly = true)
    public List<ComentarioDTO> executar(String noticiaId, String ordenacao, String usuarioId) {
        noticiaRepository.buscarPorId(noticiaId)
                .orElseThrow(() -> new NoticiaNaoEncontradaException(noticiaId));

        var comentarios = comentarioRepository.listarPorNoticia(noticiaId, ordenacao);
        if (comentarios.isEmpty()) return List.of();

        List<String> ids = comentarios.stream().map(Comentario::getId).toList();

        Map<String, Usuario> autores = comentarios.stream()
                .map(c -> c.getAutorId())
                .distinct()
                .map(id -> usuarioRepository.buscarPorId(id))
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get())
                .collect(Collectors.toMap(Usuario::getId, u -> u));

        Map<String, Long> likeCounts = reacaoRepository.contarPorAlvos(AlvoTipo.COMENTARIO, ids);
        Set<String> likedByMe = reacaoRepository.filtrarLikedByUsuario(usuarioId, AlvoTipo.COMENTARIO, ids);

        return comentarios.stream()
                .map(c -> {
                    var autor = autores.get(c.getAutorId());
                    String nome = autor != null ? autor.getNome() : "Usuário";
                    String avatar = autor != null ? autor.getAvatarUrl() : null;
                    long total = likeCounts.getOrDefault(c.getId(), 0L);
                    boolean liked = likedByMe.contains(c.getId());
                    return ComentarioDTO.from(c, nome, avatar, total, liked);
                })
                .collect(Collectors.toList());
    }
}
