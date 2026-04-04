package com.noticiapopular.comentarios.application.ports.out;

import com.noticiapopular.comentarios.domain.entities.Comentario;

import java.util.List;
import java.util.Optional;

public interface ComentarioRepositoryPort {

    Comentario salvar(Comentario comentario);

    List<Comentario> listarPorNoticia(String noticiaId);

    Optional<Comentario> buscarPorId(String id);

    void excluir(String id);

    long contarPorNoticia(String noticiaId);
}
