package com.noticiapopular.categorias.infrastructure.persistence.repositories;

import com.noticiapopular.categorias.application.ports.out.CategoriaRepositoryPort;
import com.noticiapopular.categorias.domain.entities.Categoria;
import com.noticiapopular.categorias.infrastructure.persistence.mappers.CategoriaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CategoriaRepositoryAdapter implements CategoriaRepositoryPort {

    private final CategoriaJpaRepository jpaRepository;
    private final CategoriaMapper categoriaMapper;

    @Override
    public Categoria salvar(Categoria categoria) {
        var entity = categoriaMapper.toEntity(categoria);
        return categoriaMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Categoria> buscarPorId(String id) {
        return jpaRepository.findById(id).map(categoriaMapper::toDomain);
    }

    @Override
    public Optional<Categoria> buscarPorSlug(String slug) {
        return jpaRepository.findBySlug(slug).map(categoriaMapper::toDomain);
    }

    @Override
    public List<Categoria> listarAtivas() {
        return jpaRepository.findByAtivaOrderByOrdemAsc(true).stream()
                .map(categoriaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Categoria> listarTodas() {
        return jpaRepository.findAllByOrderByOrdemAsc().stream()
                .map(categoriaMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existePorNome(String nome) {
        return jpaRepository.existsByNomeIgnoreCase(nome);
    }

    @Override
    public void excluir(String id) {
        jpaRepository.deleteById(id);
    }
}
