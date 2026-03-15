package com.noticiapopular.noticias.application.usecases;

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

    @Transactional(readOnly = true)
    public Page<NoticiaResumoDTO> executar(String autorId, Pageable pageable) {
        return noticiaRepository.listarPorAutor(autorId, pageable)
                .map(NoticiaResumoDTO::from);
    }
}
