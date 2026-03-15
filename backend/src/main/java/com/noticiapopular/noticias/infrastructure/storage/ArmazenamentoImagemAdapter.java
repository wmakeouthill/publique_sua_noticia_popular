package com.noticiapopular.noticias.infrastructure.storage;

import com.noticiapopular.noticias.application.ports.out.ArmazenamentoImagemPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Component
public class ArmazenamentoImagemAdapter implements ArmazenamentoImagemPort {

    @Value("${app.upload.diretorio:./uploads}")
    private String diretorioUpload;

    @Value("${app.upload.tamanho-maximo-mb:10}")
    private int tamanhoMaximoMb;

    @Override
    public String salvar(byte[] dados, String nomeOriginal, String contentType) {
        try {
            Path diretorio = Paths.get(diretorioUpload);
            Files.createDirectories(diretorio);

            String extensao = obterExtensao(nomeOriginal);
            String nomeArquivo = UUID.randomUUID() + extensao;
            Path caminhoArquivo = diretorio.resolve(nomeArquivo);

            Files.write(caminhoArquivo, dados);
            log.info("Imagem salva: {}", caminhoArquivo);

            return nomeArquivo;
        } catch (IOException ex) {
            log.error("Erro ao salvar imagem", ex);
            throw new RuntimeException("Falha ao salvar imagem: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void excluir(String caminhoImagem) {
        try {
            Path caminho = Paths.get(diretorioUpload).resolve(caminhoImagem);
            Files.deleteIfExists(caminho);
        } catch (IOException ex) {
            log.warn("Falha ao excluir imagem: {}", caminhoImagem, ex);
        }
    }

    @Override
    public String obterUrlPublica(String caminhoImagem) {
        return "/uploads/" + caminhoImagem;
    }

    private String obterExtensao(String nomeOriginal) {
        if (nomeOriginal == null || !nomeOriginal.contains(".")) {
            return ".jpg";
        }
        return nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
    }
}
