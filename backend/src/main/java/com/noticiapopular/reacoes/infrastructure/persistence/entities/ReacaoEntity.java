package com.noticiapopular.reacoes.infrastructure.persistence.entities;

import com.noticiapopular.reacoes.domain.valueobjects.AlvoTipo;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "reacoes", indexes = {
        @Index(name = "idx_reacao_alvo", columnList = "alvo_tipo, alvo_id"),
        @Index(name = "idx_reacao_usuario_id", columnList = "usuario_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_reacao_usuario_alvo", columnNames = {"usuario_id", "alvo_tipo", "alvo_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ReacaoEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(name = "usuario_id", nullable = false)
    private String usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "alvo_tipo", nullable = false, length = 20)
    private AlvoTipo alvoTipo;

    @Column(name = "alvo_id", nullable = false)
    private String alvoId;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;
}
