package com.noticiapopular.comentarios.infrastructure.persistence.repositories;

import com.noticiapopular.comentarios.application.ports.out.ComentarioRepositoryPort;
import com.noticiapopular.comentarios.domain.entities.Comentario;
import com.noticiapopular.comentarios.infrastructure.persistence.mappers.ComentarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ComentarioRepositoryAdapter implements ComentarioRepositoryPort {

    private final ComentarioJpaRepository jpaRepository;
    private final ComentarioMapper mapper;

    @Override
    public Comentario salvar(Comentario comentario) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(comentario)));
    }

    @Override
    public List<Comentario> listarPorNoticia(String noticiaId) {
        return jpaRepository.findByNoticiaIdOrderByCriadoEmAsc(noticiaId)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Comentario> buscarPorId(String id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void excluir(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long contarPorNoticia(String noticiaId) {
        return jpaRepository.countByNoticiaId(noticiaId);
    }
}
