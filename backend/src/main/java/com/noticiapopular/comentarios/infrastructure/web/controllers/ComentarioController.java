package com.noticiapopular.comentarios.infrastructure.web.controllers;

import com.noticiapopular.comentarios.application.dtos.ComentarioDTO;
import com.noticiapopular.comentarios.application.dtos.CriarComentarioRequest;
import com.noticiapopular.comentarios.application.usecases.CriarComentarioUseCase;
import com.noticiapopular.comentarios.application.usecases.ExcluirComentarioUseCase;
import com.noticiapopular.comentarios.application.usecases.ListarComentariosUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/noticias/{noticiaId}/comentarios")
@RequiredArgsConstructor
public class ComentarioController {

    private final CriarComentarioUseCase criarComentario;
    private final ListarComentariosUseCase listarComentarios;
    private final ExcluirComentarioUseCase excluirComentario;

    @GetMapping
    public ResponseEntity<List<ComentarioDTO>> listar(
            @PathVariable String noticiaId,
            @RequestParam(defaultValue = "MAIS_RECENTE") String ordenacao,
            @AuthenticationPrincipal String usuarioId) {
        return ResponseEntity.ok(listarComentarios.executar(noticiaId, ordenacao, usuarioId));
    }

    @PostMapping
    public ResponseEntity<ComentarioDTO> criar(
            @PathVariable String noticiaId,
            @Valid @RequestBody CriarComentarioRequest request,
            @AuthenticationPrincipal String usuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(criarComentario.executar(noticiaId, request, usuarioId));
    }

    @DeleteMapping("/{comentarioId}")
    public ResponseEntity<Void> excluir(
            @PathVariable String noticiaId,
            @PathVariable String comentarioId,
            @AuthenticationPrincipal String usuarioId) {
        excluirComentario.executar(comentarioId, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
