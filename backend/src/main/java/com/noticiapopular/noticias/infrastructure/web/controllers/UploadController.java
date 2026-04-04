package com.noticiapopular.noticias.infrastructure.web.controllers;

import com.noticiapopular.kernel.domain.exceptions.RegraDeNegocioException;
import com.noticiapopular.noticias.application.ports.out.ArmazenamentoImagemPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadController {

    private static final Set<String> CONTENT_TYPES_PERMITIDOS =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final ArmazenamentoImagemPort armazenamentoImagem;

    @Value("${app.upload.tamanho-maximo-mb:10}")
    private int tamanhoMaximoMb;

    @PostMapping("/imagem")
    public ResponseEntity<Map<String, String>> uploadImagem(
            @RequestParam("arquivo") MultipartFile arquivo) throws IOException {

        if (arquivo.isEmpty()) {
            throw new RegraDeNegocioException("Nenhum arquivo enviado");
        }

        String contentType = arquivo.getContentType();
        if (contentType == null || !CONTENT_TYPES_PERMITIDOS.contains(contentType)) {
            throw new RegraDeNegocioException(
                    "Tipo de arquivo não permitido. Use JPEG, PNG, WebP ou GIF");
        }

        long tamanhoMaximoBytes = (long) tamanhoMaximoMb * 1024 * 1024;
        if (arquivo.getSize() > tamanhoMaximoBytes) {
            throw new RegraDeNegocioException(
                    "Arquivo muito grande. Tamanho máximo: " + tamanhoMaximoMb + "MB");
        }

        String nomeArquivo = armazenamentoImagem.salvar(
                arquivo.getBytes(),
                arquivo.getOriginalFilename(),
                contentType
        );

        String urlPublica = armazenamentoImagem.obterUrlPublica(nomeArquivo);
        log.info("Upload realizado: {}", nomeArquivo);

        return ResponseEntity.ok(Map.of("url", urlPublica, "nomeArquivo", nomeArquivo));
    }
}
