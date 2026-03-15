package com.noticiapopular.categorias.infrastructure.web.controllers;

import com.noticiapopular.categorias.application.dtos.AtualizarCategoriaRequest;
import com.noticiapopular.categorias.application.dtos.CategoriaDTO;
import com.noticiapopular.categorias.application.dtos.CriarCategoriaRequest;
import com.noticiapopular.categorias.application.usecases.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CriarCategoriaUseCase criarCategoria;
    private final ListarCategoriasUseCase listarCategorias;
    private final AtualizarCategoriaUseCase atualizarCategoria;
    private final DesativarCategoriaUseCase desativarCategoria;

    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> listar(
            @RequestParam(defaultValue = "false") boolean incluirInativas) {
        return ResponseEntity.ok(listarCategorias.executar(incluirInativas));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaDTO> criar(
            @Valid @RequestBody CriarCategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(criarCategoria.executar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaDTO> atualizar(
            @PathVariable String id,
            @Valid @RequestBody AtualizarCategoriaRequest request) {
        return ResponseEntity.ok(atualizarCategoria.executar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desativar(@PathVariable String id) {
        desativarCategoria.executar(id);
        return ResponseEntity.noContent().build();
    }
}
