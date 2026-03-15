package com.noticiapopular.categorias.application.usecases;

import com.noticiapopular.categorias.application.ports.out.CategoriaRepositoryPort;
import com.noticiapopular.categorias.domain.entities.Categoria;
import com.noticiapopular.categorias.domain.exceptions.CategoriaNaoEncontradaException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DesativarCategoriaUseCase {

    private final CategoriaRepositoryPort categoriaRepository;

    @Transactional
    public void executar(String id) {
        Categoria categoria = categoriaRepository.buscarPorId(id)
                .orElseThrow(() -> new CategoriaNaoEncontradaException(id));

        categoria.desativar();
        categoriaRepository.salvar(categoria);
    }
}
