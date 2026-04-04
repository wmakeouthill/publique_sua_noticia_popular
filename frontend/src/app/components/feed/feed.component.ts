import { Component, signal, inject, OnInit, computed, ViewChild, ElementRef, AfterViewInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { useCategorias } from './composables/use-categorias';
import { NoticiaService } from '../../services/noticia.service';
import { NoticiaResumo } from '../../models/noticia.model';
import { Categoria } from '../../models/categoria.model';
import { NoticiaCardComponent } from '../../shared/components/noticia-card/noticia-card.component';
import { firstValueFrom } from 'rxjs';

type Ordenacao = 'MAIS_RECENTE' | 'MAIS_ANTIGO' | 'MAIS_VISTO' | 'MAIS_CURTIDO';

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [CommonModule, NoticiaCardComponent],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.css'
})
export class FeedComponent implements OnInit, AfterViewInit {
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

  readonly ordenacaoAtiva = signal<Ordenacao>('MAIS_RECENTE');
  readonly sortDropdownOpen = signal(false);

  readonly ordenacaoOpcoes: { valor: Ordenacao; label: string; icone: string }[] = [
    { valor: 'MAIS_RECENTE',  label: 'Mais recentes', icone: '🕐' },
    { valor: 'MAIS_ANTIGO',   label: 'Mais antigos',  icone: '📅' },
    { valor: 'MAIS_VISTO',    label: 'Mais vistos',   icone: '🔥' },
    { valor: 'MAIS_CURTIDO',  label: 'Mais curtidos', icone: '❤️' },
  ];

  readonly ordenacaoAtualLabel = computed(() =>
    this.ordenacaoOpcoes.find(o => o.valor === this.ordenacaoAtiva())?.label ?? 'Ordenar'
  );

  @ViewChild('filtroContainer') filtroContainer!: ElementRef<HTMLDivElement>;

  private isDragging = false;
  private hasDragged = false;
  private startX = 0;
  private scrollLeft = 0;

  ngOnInit(): void {
    this.apiCategorias.carregar();
    this.carregarNoticias();
  }

  ngAfterViewInit(): void {
    const el = this.filtroContainer.nativeElement;

    el.addEventListener('mousedown', (e: MouseEvent) => {
      this.isDragging = true;
      this.hasDragged = false;
      this.startX = e.pageX - el.offsetLeft;
      this.scrollLeft = el.scrollLeft;
      el.style.cursor = 'grabbing';
    });

    el.addEventListener('mouseleave', () => {
      this.isDragging = false;
      el.style.cursor = 'grab';
    });

    el.addEventListener('mouseup', () => {
      this.isDragging = false;
      el.style.cursor = 'grab';
    });

    el.addEventListener('mousemove', (e: MouseEvent) => {
      if (!this.isDragging) return;
      e.preventDefault();
      const x = e.pageX - el.offsetLeft;
      const walk = (x - this.startX) * 1.5;
      if (Math.abs(x - this.startX) > 5) this.hasDragged = true;
      el.scrollLeft = this.scrollLeft - walk;
    });

    // Intercept click during drag (capture phase fires before Angular handlers)
    el.addEventListener('click', (e: MouseEvent) => {
      if (this.hasDragged) {
        e.stopPropagation();
        this.hasDragged = false;
      }
    }, true);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.sort-container')) {
      this.sortDropdownOpen.set(false);
    }
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
        this.noticiaService.feed(
          this.pagina(), 12,
          this.categoriaAtiva() || undefined,
          undefined,
          this.ordenacaoAtiva()
        )
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

  ordenarPor(ordenacao: Ordenacao): void {
    this.ordenacaoAtiva.set(ordenacao);
    this.sortDropdownOpen.set(false);
    this.carregarNoticias(true);
  }

  toggleSortDropdown(event: MouseEvent): void {
    event.stopPropagation();
    this.sortDropdownOpen.update(v => !v);
  }

  carregarMais(): void {
    if (!this.ultimaPagina() && !this.carregandoNoticias()) {
      this.pagina.update(p => p + 1);
      this.carregarNoticias(false);
    }
  }
}
