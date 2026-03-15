package com.noticiapopular.categorias.application.usecases;

import com.noticiapopular.categorias.application.dtos.CategoriaDTO;
import com.noticiapopular.categorias.application.ports.out.CategoriaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarCategoriasUseCase {

    private final CategoriaRepositoryPort categoriaRepository;

    @Transactional(readOnly = true)
    public List<CategoriaDTO> executar(boolean incluirInativas) {
        var categorias = incluirInativas
                ? categoriaRepository.listarTodas()
                : categoriaRepository.listarAtivas();

        return categorias.stream()
                .map(CategoriaDTO::from)
                .toList();
    }
}
