import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { NoticiaService } from '../../services/noticia.service';
import { CategoriaService } from '../../services/categoria.service';
import { AuthService } from '../../services/auth.service';
import { ReacaoService } from '../../services/reacao.service';
import { Noticia } from '../../models/noticia.model';
import { Categoria } from '../../models/categoria.model';
import { TempoRelativoPipe } from '../../shared/pipes/tempo-relativo.pipe';
import { ComentariosComponent } from '../comentarios/comentarios.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-noticia-detalhe',
  standalone: true,
  imports: [CommonModule, RouterLink, TempoRelativoPipe, ComentariosComponent],
  templateUrl: './noticia-detalhe.component.html',
  styleUrl: './noticia-detalhe.component.css'
})
export class NoticiaDetalheComponent implements OnInit {
  private readonly noticiaService = inject(NoticiaService);
  private readonly categoriaService = inject(CategoriaService);
  readonly authService = inject(AuthService);
  private readonly reacaoService = inject(ReacaoService);
  private readonly route = inject(ActivatedRoute);

  readonly noticia = signal<Noticia | null>(null);
  readonly categoria = signal<Categoria | null>(null);
  readonly carregando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly totalLikes = signal(0);
  readonly likedByMe = signal(false);
  readonly toggling = signal(false);

  // Computed para verificar se o usuário atual é o autor da notícia logado
  readonly coverObjectPosition = computed(() => {
    const url = this.noticia()?.imagemUrl ?? '';
    const coverY = new URL(url, 'http://x').searchParams.get('coverY');
    const y = coverY ? parseInt(coverY, 10) : 50;
    return `center ${y}%`;
  });

  readonly isAutor = computed(() => {
    const usuarioAtual = this.authService.usuarioAtual();
    const noticiaAtual = this.noticia();
    return usuarioAtual && noticiaAtual && usuarioAtual.id === noticiaAtual.autorId;
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.carregarNoticia(id);
    }
  }

  async carregarNoticia(id: string): Promise<void> {
    this.carregando.set(true);
    this.erro.set(null);
    try {
      const dbNoticia = await firstValueFrom(this.noticiaService.buscarPorId(id));
      this.noticia.set(dbNoticia);
      this.totalLikes.set(dbNoticia.totalLikes ?? 0);
      this.likedByMe.set(dbNoticia.likedByMe ?? false);

      // Busca categorias para exibir o nome no detalhe
      const cats = await firstValueFrom(this.categoriaService.buscarCategorias());
      const cat = cats.find(c => c.id === dbNoticia.categoriaId) ?? null;
      this.categoria.set(cat);
    } catch {
      this.erro.set('Não foi possível carregar a notícia. Ela pode ter sido removida ou não existir.');
    } finally {
      this.carregando.set(false);
    }
  }

  async toggleLike(): Promise<void> {
    if (!this.authService.autenticado() || this.toggling() || !this.noticia()) return;
    this.toggling.set(true);
    try {
      const status = await firstValueFrom(
        this.reacaoService.toggle({ alvoTipo: 'NOTICIA', alvoId: this.noticia()!.id })
      );
      this.totalLikes.set(status.total);
      this.likedByMe.set(status.likedByMe);
    } finally {
      this.toggling.set(false);
    }
  }

  getHtmlConteudo(conteudoJsonString: string): string {
    if (!conteudoJsonString) return '';
    try {
      const json = JSON.parse(conteudoJsonString);
      if (json.blocks) {
        let h = '';
        json.blocks.forEach((block: any) => {
          const text = block.data?.text ?? '';
          if (block.type === 'paragraph') {
            h += `<p>${text}</p>`;
          } else if (block.type === 'h1') {
            h += `<h1>${text}</h1>`;
          } else if (block.type === 'h2') {
            h += `<h2>${text}</h2>`;
          } else if (block.type === 'h3') {
            h += `<h3>${text}</h3>`;
          } else if (block.type === 'quote') {
            h += `<blockquote>${text}</blockquote>`;
          } else if (block.type === 'list') {
            const items: string[] = text ? text.split('\n').filter((i: string) => i) : (block.data?.items ?? []);
            h += `<ul>${items.map((i: string) => `<li>${i}</li>`).join('')}</ul>`;
          } else if (block.type === 'image' && text) {
            const layout = block.data?.layout ?? 'full';
            const width  = block.data?.width  ?? 100;
            const style  = layout === 'left'  ? `float:left;width:${width}%;margin:0 1.5rem 1rem 0`
                         : layout === 'right' ? `float:right;width:${width}%;margin:0 0 1rem 1.5rem`
                         : 'width:100%;display:block';
            const clear  = layout !== 'full' ? '<div style="clear:both"></div>' : '';
            h += `<figure style="margin:1rem 0"><img src="${text}" style="${style};border-radius:8px;max-width:100%" /></figure>${clear}`;
          }
        });
        return h;
      }
      return conteudoJsonString;
    } catch(e) {
      return `<p>${conteudoJsonString}</p>`;
    }
  }
}
