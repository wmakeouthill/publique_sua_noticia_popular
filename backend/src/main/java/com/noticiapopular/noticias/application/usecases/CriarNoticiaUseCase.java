package com.noticiapopular.noticias.application.usecases;

import com.noticiapopular.categorias.application.ports.out.CategoriaRepositoryPort;
import com.noticiapopular.categorias.domain.exceptions.CategoriaNaoEncontradaException;
import com.noticiapopular.noticias.application.dtos.CriarNoticiaRequest;
import com.noticiapopular.noticias.application.dtos.NoticiaDTO;
import com.noticiapopular.noticias.application.ports.out.NoticiaRepositoryPort;
import com.noticiapopular.noticias.domain.entities.Noticia;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CriarNoticiaUseCase {

    private final NoticiaRepositoryPort noticiaRepository;
    private final CategoriaRepositoryPort categoriaRepository;

    @Transactional
    public NoticiaDTO executar(CriarNoticiaRequest request, String autorId) {
        categoriaRepository.buscarPorId(request.categoriaId())
                .filter(c -> c.isAtiva())
                .orElseThrow(() -> new CategoriaNaoEncontradaException(request.categoriaId()));

        Noticia noticia = Noticia.criar(
                request.titulo(),
                request.conteudo(),
                request.resumo(),
                request.imagemUrl(),
                request.categoriaId(),
                autorId
        );

        if (request.publicarImediatamente()) {
            noticia.publicar();
        }

        return NoticiaDTO.from(noticiaRepository.salvar(noticia));
    }
}
