package com.noticiapopular.noticias.infrastructure.persistence.entities;

import com.noticiapopular.noticias.domain.valueobjects.StatusNoticia;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "noticias", indexes = {
        @Index(name = "idx_noticia_categoria_id", columnList = "categoria_id"),
        @Index(name = "idx_noticia_autor_id", columnList = "autor_id"),
        @Index(name = "idx_noticia_status", columnList = "status"),
        @Index(name = "idx_noticia_publicado_em", columnList = "publicado_em"),
        @Index(name = "idx_noticia_status_publicado_em", columnList = "status, publicado_em")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class NoticiaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(length = 500)
    private String resumo;

    @Column(name = "imagem_url", length = 500)
    private String imagemUrl;

    @Column(name = "categoria_id", nullable = false)
    private String categoriaId;

    @Column(name = "autor_id", nullable = false)
    private String autorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusNoticia status = StatusNoticia.RASCUNHO;

    @Column(nullable = false)
    @Builder.Default
    private long visualizacoes = 0L;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em")
    private Instant atualizadoEm;

    @Column(name = "publicado_em")
    private Instant publicadoEm;
}
