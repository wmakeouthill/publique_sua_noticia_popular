package com.noticiapopular.noticias.application.usecases;

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

    @Transactional
    public NoticiaDTO executar(String id) {
        Noticia noticia = noticiaRepository.buscarPorId(id)
                .orElseThrow(() -> new NoticiaNaoEncontradaException(id));

        noticia.incrementarVisualizacao();
        noticiaRepository.salvar(noticia);

        return NoticiaDTO.from(noticia);
    }
}
