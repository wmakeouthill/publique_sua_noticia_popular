package com.noticiapopular.categorias.application.usecases;

import com.noticiapopular.categorias.application.dtos.AtualizarCategoriaRequest;
import com.noticiapopular.categorias.application.dtos.CategoriaDTO;
import com.noticiapopular.categorias.application.ports.out.CategoriaRepositoryPort;
import com.noticiapopular.categorias.domain.entities.Categoria;
import com.noticiapopular.categorias.domain.exceptions.CategoriaNaoEncontradaException;
import com.noticiapopular.kernel.domain.exceptions.RegraDeNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AtualizarCategoriaUseCase {

    private final CategoriaRepositoryPort categoriaRepository;

    @Transactional
    public CategoriaDTO executar(String id, AtualizarCategoriaRequest request) {
        Categoria categoria = categoriaRepository.buscarPorId(id)
                .orElseThrow(() -> new CategoriaNaoEncontradaException(id));

        if (request.nome() != null && !request.nome().equals(categoria.getNome())) {
            if (categoriaRepository.existePorNome(request.nome())) {
                throw new RegraDeNegocioException("Já existe uma categoria com o nome: " + request.nome());
            }
            categoria.atualizarNome(request.nome());
        }

        if (request.descricao() != null) {
            categoria.atualizarDescricao(request.descricao());
        }

        if (request.icone() != null) {
            categoria.atualizarIcone(request.icone());
        }

        if (request.ordem() != null) {
            categoria.definirOrdem(request.ordem());
        }

        return CategoriaDTO.from(categoriaRepository.salvar(categoria));
    }
}
