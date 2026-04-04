package com.noticiapopular.noticias.infrastructure.persistence.repositories;

import com.noticiapopular.noticias.application.ports.out.NoticiaRepositoryPort;
import com.noticiapopular.noticias.domain.entities.Noticia;
import com.noticiapopular.noticias.domain.valueobjects.StatusNoticia;
import com.noticiapopular.noticias.infrastructure.persistence.mappers.NoticiaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NoticiaRepositoryAdapter implements NoticiaRepositoryPort {

    private final NoticiaJpaRepository jpaRepository;
    private final NoticiaMapper noticiaMapper;

    @Override
    public Noticia salvar(Noticia noticia) {
        var entity = noticiaMapper.toEntity(noticia);
        return noticiaMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Noticia> buscarPorId(String id) {
        return jpaRepository.findById(id).map(noticiaMapper::toDomain);
    }

    @Override
    public Page<Noticia> listarPublicadas(String categoriaId, String busca, Pageable pageable) {
        if (!StringUtils.hasText(busca)) {
            return jpaRepository.findPublicadasSemBusca(categoriaId, pageable)
                    .map(noticiaMapper::toDomain);
        }

        return jpaRepository.findPublicadasComBusca(categoriaId, busca, pageable)
                .map(noticiaMapper::toDomain);
    }

    @Override
    public Page<Noticia> listarPublicadasPorLikes(String categoriaId, String busca, Pageable pageable) {
        if (!StringUtils.hasText(busca)) {
            return jpaRepository.findPublicadasOrderByLikes(categoriaId, pageable)
                    .map(noticiaMapper::toDomain);
        }
        return jpaRepository.findPublicadasComBuscaOrderByLikes(categoriaId, busca, pageable)
                .map(noticiaMapper::toDomain);
    }

    @Override
    public Page<Noticia> listarPorAutor(String autorId, Pageable pageable) {
        return jpaRepository.findByAutorIdOrderByCriadoEmDesc(autorId, pageable)
                .map(noticiaMapper::toDomain);
    }

    @Override
    public void excluir(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long contarPorStatus(StatusNoticia status) {
        return jpaRepository.countByStatus(status);
    }

    @Override
    public long contarPorCategoria(String categoriaId) {
        return jpaRepository.countByCategoriaId(categoriaId);
    }
}
