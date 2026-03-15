package com.noticiapopular.categorias.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "categorias", indexes = {
        @Index(name = "idx_categoria_slug", columnList = "slug"),
        @Index(name = "idx_categoria_ativa", columnList = "ativa")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CategoriaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(length = 500)
    private String descricao;

    @Column(length = 10)
    private String icone;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativa = true;

    @Column(nullable = false)
    @Builder.Default
    private int ordem = 0;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;
}
