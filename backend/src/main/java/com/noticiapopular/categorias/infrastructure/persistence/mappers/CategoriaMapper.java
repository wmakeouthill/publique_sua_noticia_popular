package com.noticiapopular.categorias.infrastructure.persistence.mappers;

import com.noticiapopular.categorias.domain.entities.Categoria;
import com.noticiapopular.categorias.infrastructure.persistence.entities.CategoriaEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {

    public CategoriaEntity toEntity(Categoria categoria) {
        return CategoriaEntity.builder()
                .id(categoria.getId())
                .nome(categoria.getNome())
                .slug(categoria.getSlug())
                .descricao(categoria.getDescricao())
                .icone(categoria.getIcone())
                .ativa(categoria.isAtiva())
                .ordem(categoria.getOrdem())
                .criadoEm(categoria.getCriadoEm())
                .build();
    }

    public Categoria toDomain(CategoriaEntity entity) {
        return Categoria.reconstituir(
                entity.getId(),
                entity.getNome(),
                entity.getSlug(),
                entity.getDescricao(),
                entity.getIcone(),
                entity.isAtiva(),
                entity.getOrdem(),
                entity.getCriadoEm()
        );
    }
}
