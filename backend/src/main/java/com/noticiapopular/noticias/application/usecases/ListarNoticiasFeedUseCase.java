package com.noticiapopular.noticias.application.usecases;

import com.noticiapopular.noticias.application.dtos.FiltroNoticiaRequest;
import com.noticiapopular.noticias.application.dtos.NoticiaResumoDTO;
import com.noticiapopular.noticias.application.ports.out.NoticiaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarNoticiasFeedUseCase {

    private final NoticiaRepositoryPort noticiaRepository;

    @Transactional(readOnly = true)
    public Page<NoticiaResumoDTO> executar(FiltroNoticiaRequest filtro, Pageable pageable) {
        return noticiaRepository
                .listarPublicadas(filtro.categoriaId(), filtro.busca(), pageable)
                .map(NoticiaResumoDTO::from);
    }
}
