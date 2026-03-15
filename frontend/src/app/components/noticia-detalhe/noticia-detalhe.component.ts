import { Component, inject, signal, effect, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { NoticiaService } from '../../services/noticia.service';
import { AuthService } from '../../services/auth.service';
import { Noticia } from '../../models/noticia.model';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-noticia-detalhe',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './noticia-detalhe.component.html',
  styleUrl: './noticia-detalhe.component.css'
})
export class NoticiaDetalheComponent {
  private readonly noticiaService = inject(NoticiaService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  readonly noticia = signal<Noticia | null>(null);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);

  // Computed para verificar se o usuário atual é o autor da notícia logado
  readonly isAutor = computed(() => {
    const usuarioAtual = this.authService.usuarioAtual();
    const noticiaAtual = this.noticia();
    return usuarioAtual && noticiaAtual && usuarioAtual.id === noticiaAtual.autorId;
  });

  constructor() {
    effect(() => {
      const id = this.route.snapshot.paramMap.get('id');
      if (id) {
        this.carregarNoticia(id);
      }
    });
  }

  async carregarNoticia(id: string): Promise<void> {
    this.carregando.set(true);
    this.erro.set(null);
    try {
      const dbNoticia = await firstValueFrom(this.noticiaService.buscarPorId(id));
      this.noticia.set(dbNoticia);
    } catch(e) {
      this.erro.set('Não foi possível carregar a notícia. Ela pode ter sido removida ou não existir.');
    } finally {
      this.carregando.set(false);
    }
  }

  getHtmlConteudo(conteudoJsonString: string): string {
    if (!conteudoJsonString) return '';
    try {
      const json = JSON.parse(conteudoJsonString);
      if (json.blocks) {
        let h = '';
        json.blocks.forEach((block: any) => {
           if(block.type === 'paragraph') h += `<p>${block.data.text}</p>`;
           if(block.type === 'header') h += `<h${block.data.level}>${block.data.text}</h${block.data.level}>`;
           if(block.type === 'list') {
              h += '<ul>';
              block.data.items.forEach((item: string) => h += `<li>${item}</li>`);
              h += '</ul>';
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
