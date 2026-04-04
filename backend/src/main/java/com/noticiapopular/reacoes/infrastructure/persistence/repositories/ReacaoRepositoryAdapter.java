package com.noticiapopular.reacoes.infrastructure.persistence.repositories;

import com.noticiapopular.reacoes.application.ports.out.ReacaoRepositoryPort;
import com.noticiapopular.reacoes.domain.entities.Reacao;
import com.noticiapopular.reacoes.domain.valueobjects.AlvoTipo;
import com.noticiapopular.reacoes.infrastructure.persistence.mappers.ReacaoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ReacaoRepositoryAdapter implements ReacaoRepositoryPort {

    private final ReacaoJpaRepository jpaRepository;
    private final ReacaoMapper mapper;

    @Override
    public void salvar(Reacao reacao) {
        jpaRepository.save(mapper.toEntity(reacao));
    }

    @Override
    public void excluir(String usuarioId, AlvoTipo alvoTipo, String alvoId) {
        jpaRepository.deleteByUsuarioIdAndAlvoTipoAndAlvoId(usuarioId, alvoTipo, alvoId);
    }

    @Override
    public boolean existeLike(String usuarioId, AlvoTipo alvoTipo, String alvoId) {
        return jpaRepository.existsByUsuarioIdAndAlvoTipoAndAlvoId(usuarioId, alvoTipo, alvoId);
    }

    @Override
    public long contarPorAlvo(AlvoTipo alvoTipo, String alvoId) {
        return jpaRepository.countByAlvoTipoAndAlvoId(alvoTipo, alvoId);
    }

    @Override
    public Map<String, Long> contarPorAlvos(AlvoTipo alvoTipo, Collection<String> alvoIds) {
        if (alvoIds == null || alvoIds.isEmpty()) return Map.of();
        Map<String, Long> resultado = new HashMap<>();
        jpaRepository.countGroupedByAlvoId(alvoTipo, alvoIds)
                .forEach(row -> resultado.put((String) row[0], (Long) row[1]));
        return resultado;
    }

    @Override
    public Set<String> filtrarLikedByUsuario(String usuarioId, AlvoTipo alvoTipo, Collection<String> alvoIds) {
        if (usuarioId == null || alvoIds == null || alvoIds.isEmpty()) return Set.of();
        return new HashSet<>(jpaRepository.findAlvoIdsLikedByUsuario(usuarioId, alvoTipo, alvoIds));
    }
}
