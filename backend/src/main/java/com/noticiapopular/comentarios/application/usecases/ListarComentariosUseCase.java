package com.noticiapopular.comentarios.application.usecases;

import com.noticiapopular.autenticacao.application.ports.out.UsuarioRepositoryPort;
import com.noticiapopular.autenticacao.domain.entities.Usuario;
import com.noticiapopular.comentarios.application.dtos.ComentarioDTO;
import com.noticiapopular.comentarios.application.ports.out.ComentarioRepositoryPort;
import com.noticiapopular.noticias.application.ports.out.NoticiaRepositoryPort;
import com.noticiapopular.noticias.domain.exceptions.NoticiaNaoEncontradaException;
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

    @Transactional(readOnly = true)
    public List<ComentarioDTO> executar(String noticiaId) {
        noticiaRepository.buscarPorId(noticiaId)
                .orElseThrow(() -> new NoticiaNaoEncontradaException(noticiaId));

        var comentarios = comentarioRepository.listarPorNoticia(noticiaId);
        if (comentarios.isEmpty()) return List.of();

        Set<String> autorIds = comentarios.stream()
                .map(c -> c.getAutorId())
                .collect(Collectors.toSet());

        Map<String, Usuario> autores = authorIds(autorIds);

        return comentarios.stream()
                .map(c -> {
                    var autor = autores.get(c.getAutorId());
                    String nome = autor != null ? autor.getNome() : "Usuário";
                    String avatar = autor != null ? autor.getAvatarUrl() : null;
                    return ComentarioDTO.from(c, nome, avatar);
                })
                .collect(Collectors.toList());
    }

    private Map<String, Usuario> authorIds(Set<String> ids) {
        return ids.stream()
                .map(id -> usuarioRepository.buscarPorId(id))
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get())
                .collect(Collectors.toMap(Usuario::getId, u -> u));
    }
}
