import { Component, signal, inject, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { useCategorias } from './composables/use-categorias';
import { NoticiaService } from '../../services/noticia.service';
import { NoticiaResumo } from '../../models/noticia.model';
import { Categoria } from '../../models/categoria.model';
import { NoticiaCardComponent } from '../../shared/components/noticia-card/noticia-card.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [CommonModule, NoticiaCardComponent],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.css'
})
export class FeedComponent implements OnInit {
  private readonly noticiaService = inject(NoticiaService);
  readonly apiCategorias = useCategorias();

  readonly categorias = this.apiCategorias.categorias;
  readonly categoriaAtiva = signal<string | null>(null);

  readonly categoriasMap = computed(() => {
    const map = new Map<string, Categoria>();
    this.categorias().forEach(c => map.set(c.id, c));
    return map;
  });

  readonly noticias = signal<NoticiaResumo[]>([]);
  readonly carregandoNoticias = signal(false);
  readonly erroNoticias = signal(false);
  readonly pagina = signal(0);
  readonly ultimaPagina = signal(false);

  ngOnInit(): void {
    this.apiCategorias.carregar();
    this.carregarNoticias();
  }

  async carregarNoticias(novaBusca = true): Promise<void> {
    if (novaBusca) {
      this.pagina.set(0);
      this.noticias.set([]);
      this.erroNoticias.set(false);
    }

    this.carregandoNoticias.set(true);
    try {
      const page = await firstValueFrom(
        this.noticiaService.feed(this.pagina(), 12, this.categoriaAtiva() || undefined)
      );

      this.noticias.update(lista => novaBusca ? page.content : [...lista, ...page.content]);
      this.ultimaPagina.set(page.last);
    } catch (e) {
      console.error('Falha ao carregar noticias', e);
      this.erroNoticias.set(true);
    } finally {
      this.carregandoNoticias.set(false);
    }
  }

  onFiltrarCategoria(categoriaId: string | null): void {
    if (this.categoriaAtiva() === categoriaId) {
      this.categoriaAtiva.set(null);
    } else {
      this.categoriaAtiva.set(categoriaId);
    }
    this.carregarNoticias(true);
  }

  carregarMais(): void {
    if (!this.ultimaPagina() && !this.carregandoNoticias()) {
      this.pagina.update(p => p + 1);
      this.carregarNoticias(false);
    }
  }
}
