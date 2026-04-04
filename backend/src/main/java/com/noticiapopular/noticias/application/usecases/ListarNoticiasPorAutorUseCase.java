package com.noticiapopular.noticias.application.usecases;

import com.noticiapopular.autenticacao.application.ports.out.UsuarioRepositoryPort;
import com.noticiapopular.autenticacao.domain.entities.Usuario;
import com.noticiapopular.noticias.application.dtos.NoticiaResumoDTO;
import com.noticiapopular.noticias.application.ports.out.NoticiaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarNoticiasPorAutorUseCase {

    private final NoticiaRepositoryPort noticiaRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    @Transactional(readOnly = true)
    public Page<NoticiaResumoDTO> executar(String autorId, Pageable pageable) {
        Usuario autor = usuarioRepository.buscarPorId(autorId).orElse(null);
        String nome = autor != null ? autor.getNome() : null;
        String avatar = autor != null ? autor.getAvatarUrl() : null;
        return noticiaRepository.listarPorAutor(autorId, pageable)
                .map(n -> NoticiaResumoDTO.from(n, nome, avatar));
    }
}
