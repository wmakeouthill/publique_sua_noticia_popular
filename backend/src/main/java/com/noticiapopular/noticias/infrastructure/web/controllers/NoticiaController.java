package com.noticiapopular.noticias.infrastructure.web.controllers;

import com.noticiapopular.noticias.application.dtos.*;
import com.noticiapopular.noticias.application.usecases.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/noticias")
@RequiredArgsConstructor
public class NoticiaController {

    private final CriarNoticiaUseCase criarNoticia;
    private final EditarNoticiaUseCase editarNoticia;
    private final ExcluirNoticiaUseCase excluirNoticia;
    private final BuscarNoticiaPorIdUseCase buscarNoticiaPorId;
    private final ListarNoticiasFeedUseCase listarNoticiasFeed;
    private final ListarNoticiasPorAutorUseCase listarNoticiasPorAutor;

    @GetMapping
    public ResponseEntity<Page<NoticiaResumoDTO>> feed(
            @RequestParam(required = false) String categoriaId,
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "12") int tamanho) {

        var filtro = new FiltroNoticiaRequest(categoriaId, busca);
        var pageable = PageRequest.of(pagina, tamanho, Sort.by("publicadoEm").descending());

        return ResponseEntity.ok(listarNoticiasFeed.executar(filtro, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticiaDTO> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(buscarNoticiaPorId.executar(id));
    }

    @GetMapping("/minhas")
    public ResponseEntity<Page<NoticiaResumoDTO>> minhasNoticias(
            @AuthenticationPrincipal String usuarioId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "12") int tamanho) {

        var pageable = PageRequest.of(pagina, tamanho);
        return ResponseEntity.ok(listarNoticiasPorAutor.executar(usuarioId, pageable));
    }

    @PostMapping
    public ResponseEntity<NoticiaDTO> criar(
            @Valid @RequestBody CriarNoticiaRequest request,
            @AuthenticationPrincipal String usuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(criarNoticia.executar(request, usuarioId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoticiaDTO> editar(
            @PathVariable String id,
            @Valid @RequestBody EditarNoticiaRequest request,
            @AuthenticationPrincipal String usuarioId) {
        return ResponseEntity.ok(editarNoticia.executar(id, request, usuarioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable String id,
            @AuthenticationPrincipal String usuarioId) {
        excluirNoticia.executar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
