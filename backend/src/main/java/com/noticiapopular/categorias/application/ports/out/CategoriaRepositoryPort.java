package com.noticiapopular.categorias.application.ports.out;

import com.noticiapopular.categorias.domain.entities.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepositoryPort {

    Categoria salvar(Categoria categoria);

    Optional<Categoria> buscarPorId(String id);

    Optional<Categoria> buscarPorSlug(String slug);

    List<Categoria> listarAtivas();

    List<Categoria> listarTodas();

    boolean existePorNome(String nome);

    void excluir(String id);
}
