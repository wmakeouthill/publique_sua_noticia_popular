package com.noticiapopular.comentarios.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "comentarios", indexes = {
        @Index(name = "idx_comentario_noticia_id", columnList = "noticia_id"),
        @Index(name = "idx_comentario_autor_id", columnList = "autor_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ComentarioEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(name = "noticia_id", nullable = false)
    private String noticiaId;

    @Column(name = "autor_id", nullable = false)
    private String autorId;

    @Column(nullable = false, length = 1000)
    private String conteudo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;
}
