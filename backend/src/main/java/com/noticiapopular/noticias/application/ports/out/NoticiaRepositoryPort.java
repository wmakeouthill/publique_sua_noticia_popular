package com.noticiapopular.noticias.application.ports.out;

import com.noticiapopular.noticias.domain.entities.Noticia;
import com.noticiapopular.noticias.domain.valueobjects.StatusNoticia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NoticiaRepositoryPort {

    Noticia salvar(Noticia noticia);

    Optional<Noticia> buscarPorId(String id);

    Page<Noticia> listarPublicadas(String categoriaId, String busca, Pageable pageable);

    Page<Noticia> listarPorAutor(String autorId, Pageable pageable);

    void excluir(String id);

    long contarPorStatus(StatusNoticia status);

    long contarPorCategoria(String categoriaId);
}
