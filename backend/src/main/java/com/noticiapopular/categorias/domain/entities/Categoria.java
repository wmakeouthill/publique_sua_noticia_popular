package com.noticiapopular.categorias.domain.entities;

import com.noticiapopular.categorias.domain.valueobjects.Slug;
import com.noticiapopular.kernel.domain.exceptions.ValidacaoException;

import java.time.Instant;
import java.util.UUID;

public class Categoria {

    private final String id;
    private String nome;
    private Slug slug;
    private String descricao;
    private String icone;
    private boolean ativa;
    private int ordem;
    private final Instant criadoEm;

    private Categoria(String id, String nome, Slug slug, String descricao,
                      String icone, boolean ativa, int ordem, Instant criadoEm) {
        this.id = id;
        this.nome = nome;
        this.slug = slug;
        this.descricao = descricao;
        this.icone = icone;
        this.ativa = ativa;
        this.ordem = ordem;
        this.criadoEm = criadoEm;
    }

    public static Categoria criar(String nome, String descricao, String icone, int ordem) {
        validarNome(nome);
        return new Categoria(
                UUID.randomUUID().toString(),
                nome.trim(),
                Slug.from(nome),
                descricao,
                icone,
                true,
                ordem,
                Instant.now()
        );
    }

    public static Categoria reconstituir(String id, String nome, String slug,
                                          String descricao, String icone,
                                          boolean ativa, int ordem, Instant criadoEm) {
        return new Categoria(id, nome, new Slug(slug), descricao, icone, ativa, ordem, criadoEm);
    }

    public void atualizarNome(String novoNome) {
        validarNome(novoNome);
        this.nome = novoNome.trim();
        this.slug = Slug.from(novoNome);
    }

    public void atualizarDescricao(String novaDescricao) {
        this.descricao = novaDescricao;
    }

    public void atualizarIcone(String novoIcone) {
        this.icone = novoIcone;
    }

    public void definirOrdem(int novaOrdem) {
        this.ordem = novaOrdem;
    }

    public void ativar() {
        this.ativa = true;
    }

    public void desativar() {
        this.ativa = false;
    }

    private static void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new ValidacaoException("Nome da categoria é obrigatório");
        }
        if (nome.length() > 100) {
            throw new ValidacaoException("Nome da categoria deve ter no máximo 100 caracteres");
        }
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getSlug() { return slug.valor(); }
    public String getDescricao() { return descricao; }
    public String getIcone() { return icone; }
    public boolean isAtiva() { return ativa; }
    public int getOrdem() { return ordem; }
    public Instant getCriadoEm() { return criadoEm; }
}
