package com.noticiapopular.reacoes.application.ports.out;

import com.noticiapopular.reacoes.domain.entities.Reacao;
import com.noticiapopular.reacoes.domain.valueobjects.AlvoTipo;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface ReacaoRepositoryPort {

    void salvar(Reacao reacao);

    void excluir(String usuarioId, AlvoTipo alvoTipo, String alvoId);

    boolean existeLike(String usuarioId, AlvoTipo alvoTipo, String alvoId);

    long contarPorAlvo(AlvoTipo alvoTipo, String alvoId);

    /** Retorna mapa de alvoId → contagem para múltiplos alvos de uma vez. */
    Map<String, Long> contarPorAlvos(AlvoTipo alvoTipo, Collection<String> alvoIds);

    /** Retorna o subconjunto de alvoIds em que o usuário deu like. */
    Set<String> filtrarLikedByUsuario(String usuarioId, AlvoTipo alvoTipo, Collection<String> alvoIds);
}
