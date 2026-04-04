package com.noticiapopular.admin.infrastructure.web;

import com.noticiapopular.admin.application.dtos.AdminDashboardDTO;
import com.noticiapopular.autenticacao.application.dtos.PerfilUsuarioDTO;
import com.noticiapopular.autenticacao.application.ports.out.UsuarioRepositoryPort;
import com.noticiapopular.autenticacao.domain.entities.Usuario;
import com.noticiapopular.categorias.application.ports.out.CategoriaRepositoryPort;
import com.noticiapopular.kernel.domain.exceptions.EntidadeNaoEncontradaException;
import com.noticiapopular.noticias.application.ports.out.NoticiaRepositoryPort;
import com.noticiapopular.noticias.domain.valueobjects.StatusNoticia;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UsuarioRepositoryPort usuarioRepository;
    private final NoticiaRepositoryPort noticiaRepository;
    private final CategoriaRepositoryPort categoriaRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDTO> dashboard() {
        long totalUsuarios = usuarioRepository.listarTodos().size();
        long totalNoticias = noticiaRepository.contarPorStatus(StatusNoticia.PUBLICADA)
                + noticiaRepository.contarPorStatus(StatusNoticia.RASCUNHO)
                + noticiaRepository.contarPorStatus(StatusNoticia.ARQUIVADA);
        long noticiasPublicadas = noticiaRepository.contarPorStatus(StatusNoticia.PUBLICADA);
        long noticiasRascunho = noticiaRepository.contarPorStatus(StatusNoticia.RASCUNHO);
        long totalCategorias = categoriaRepository.listarTodas().size();

        return ResponseEntity.ok(new AdminDashboardDTO(
                totalUsuarios, totalNoticias, noticiasPublicadas, noticiasRascunho, totalCategorias
        ));
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<PerfilUsuarioDTO>> listarUsuarios() {
        List<PerfilUsuarioDTO> usuarios = usuarioRepository.listarTodos().stream()
                .map(PerfilUsuarioDTO::from)
                .toList();
        return ResponseEntity.ok(usuarios);
    }

    @PatchMapping("/usuarios/{id}")
    public ResponseEntity<PerfilUsuarioDTO> alterarStatusUsuario(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> body) {

        Usuario usuario = usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado: " + id));

        Boolean ativo = body.get("ativo");
        if (ativo == null) {
            return ResponseEntity.badRequest().build();
        }

        if (ativo) {
            usuario.ativar();
        } else {
            usuario.desativar();
        }

        Usuario salvo = usuarioRepository.salvar(usuario);
        return ResponseEntity.ok(PerfilUsuarioDTO.from(salvo));
    }
}
