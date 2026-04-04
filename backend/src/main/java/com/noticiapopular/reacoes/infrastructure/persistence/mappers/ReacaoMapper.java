package com.noticiapopular.reacoes.infrastructure.persistence.mappers;

import com.noticiapopular.reacoes.domain.entities.Reacao;
import com.noticiapopular.reacoes.infrastructure.persistence.entities.ReacaoEntity;
import org.springframework.stereotype.Component;

@Component
public class ReacaoMapper {

    public ReacaoEntity toEntity(Reacao reacao) {
        return ReacaoEntity.builder()
                .id(reacao.getId())
                .usuarioId(reacao.getUsuarioId())
                .alvoTipo(reacao.getAlvoTipo())
                .alvoId(reacao.getAlvoId())
                .criadoEm(reacao.getCriadoEm())
                .build();
    }

    public Reacao toDomain(ReacaoEntity entity) {
        return Reacao.reconstituir(
                entity.getId(),
                entity.getUsuarioId(),
                entity.getAlvoTipo(),
                entity.getAlvoId(),
                entity.getCriadoEm()
        );
    }
}
