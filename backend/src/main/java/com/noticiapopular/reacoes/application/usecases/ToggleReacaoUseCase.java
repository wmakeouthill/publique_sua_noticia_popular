package com.noticiapopular.reacoes.application.usecases;

import com.noticiapopular.reacoes.application.dtos.ReacaoStatusDTO;
import com.noticiapopular.reacoes.application.ports.out.ReacaoRepositoryPort;
import com.noticiapopular.reacoes.domain.entities.Reacao;
import com.noticiapopular.reacoes.domain.valueobjects.AlvoTipo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ToggleReacaoUseCase {

    private final ReacaoRepositoryPort reacaoRepository;

    @Transactional
    public ReacaoStatusDTO executar(String usuarioId, AlvoTipo alvoTipo, String alvoId) {
        boolean jaLikeu = reacaoRepository.existeLike(usuarioId, alvoTipo, alvoId);

        if (jaLikeu) {
            reacaoRepository.excluir(usuarioId, alvoTipo, alvoId);
        } else {
            reacaoRepository.salvar(Reacao.criar(usuarioId, alvoTipo, alvoId));
        }

        long total = reacaoRepository.contarPorAlvo(alvoTipo, alvoId);
        return new ReacaoStatusDTO(total, !jaLikeu);
    }
}
