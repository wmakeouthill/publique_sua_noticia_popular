package com.noticiapopular.categorias.application.usecases;

import com.noticiapopular.categorias.application.dtos.CategoriaDTO;
import com.noticiapopular.categorias.application.dtos.CriarCategoriaRequest;
import com.noticiapopular.categorias.application.ports.out.CategoriaRepositoryPort;
import com.noticiapopular.categorias.domain.entities.Categoria;
import com.noticiapopular.kernel.domain.exceptions.RegraDeNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CriarCategoriaUseCase {

    private final CategoriaRepositoryPort categoriaRepository;

    @Transactional
    public CategoriaDTO executar(CriarCategoriaRequest request) {
        if (categoriaRepository.existePorNome(request.nome())) {
            throw new RegraDeNegocioException("Já existe uma categoria com o nome: " + request.nome());
        }

        Categoria categoria = Categoria.criar(
                request.nome(),
                request.descricao(),
                request.icone(),
                request.ordem()
        );

        return CategoriaDTO.from(categoriaRepository.salvar(categoria));
    }
}
