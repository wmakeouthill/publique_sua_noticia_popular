import { Component, inject, signal, OnInit, OnDestroy, AfterViewChecked, ViewChild, ViewChildren, QueryList, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NoticiaService } from '../../services/noticia.service';
import { IaService } from '../../services/ia.service';
import { CategoriaService } from '../../services/categoria.service';
import { NotificationService } from '../../services/notification.service';
import { UploadService } from '../../services/upload.service';
import { firstValueFrom } from 'rxjs';
import { Categoria } from '../../models/categoria.model';

interface Block {
  type: 'paragraph' | 'h1' | 'h2' | 'h3' | 'quote' | 'list';
  content: string;
}

@Component({
  selector: 'app-editor',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './editor.component.html',
  styleUrl: './editor.component.css'
})
export class EditorComponent implements OnInit, AfterViewChecked, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly noticiaService = inject(NoticiaService);
  private readonly iaService = inject(IaService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly notification = inject(NotificationService);
  private readonly uploadService = inject(UploadService);

  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;
  @ViewChildren('blockEl') blockEls!: QueryList<ElementRef>;

  // Rastreia elementos já inicializados — evita re-setar innerText durante digitação
  private readonly initializedBlocks = new WeakSet<HTMLElement>();

  readonly editandoId = signal<string | null>(null);
  readonly carregando = signal(false);
  readonly carregandoIa = signal(false);
  readonly carregandoUpload = signal(false);
  readonly categorias = signal<Categoria[]>([]);
  readonly currentBlockFocus = signal(0);
  readonly imagemPreview = signal<string | null>(null);

  readonly showIaMenu = signal<{ visible: boolean; blockIndex: number; top: number; left: number; maxHeight: number }>({
    visible: false, blockIndex: 0, top: 0, left: 0, maxHeight: 380
  });
  readonly slashMenuIndex = signal(0);

  // Ordem flat dos itens do slash menu (deve bater com a ordem no HTML)
  private readonly SLASH_FORMAT_TYPES: Block['type'][] = ['h1', 'h2', 'h3', 'quote', 'list', 'paragraph'];
  private readonly SLASH_IA_ESTILOS: Array<'formal' | 'criativo' | 'resumido'> = ['formal', 'criativo', 'resumido'];
  private get SLASH_TOTAL() { return this.SLASH_FORMAT_TYPES.length + this.SLASH_IA_ESTILOS.length; }

  // Scroll tracking para o slash menu
  private slashAnchorEl: HTMLElement | null = null;
  private slashScrollHandler: (() => void) | null = null;
  private rafId: number | null = null;
  private readonly MENU_WIDTH = 290;
  private readonly MENU_MAX_HEIGHT = 380;

  form = this.fb.group({
    titulo:      ['', [Validators.required, Validators.minLength(5)]],
    categoriaId: ['', Validators.required],
    imagemUrl:   ['']
  });

  blocks = signal<Block[]>([{ type: 'paragraph', content: '' }]);

  ngOnInit(): void {
    this.carregarCategorias();
    const paramId = this.route.snapshot.paramMap.get('id');
    if (paramId) {
      this.editandoId.set(paramId);
      this.carregarNoticia(paramId);
    }
  }

  async carregarCategorias(): Promise<void> {
    try {
      const lista = await firstValueFrom(this.categoriaService.buscarCategorias());
      this.categorias.set(lista);
      if (lista.length > 0 && !this.editandoId()) {
        this.form.patchValue({ categoriaId: lista[0].id });
      }
    } catch { /* ignora */ }
  }

  async carregarNoticia(id: string): Promise<void> {
    this.carregando.set(true);
    try {
      const noticia = await firstValueFrom(this.noticiaService.buscarPorId(id));
      this.form.patchValue({
        titulo:      noticia.titulo,
        categoriaId: noticia.categoriaId,
        imagemUrl:   noticia.imagemUrl
      });

      if (noticia.imagemUrl) {
        this.imagemPreview.set(noticia.imagemUrl);
      }

      try {
        const json = JSON.parse(noticia.conteudo);
        if (json?.blocks) {
          const parsed: Block[] = json.blocks.map((b: { type: string; data: { items?: string[]; text?: string } }) => ({
            type:    b.type as Block['type'],
            content: b.type === 'list' ? b.data.items!.join('\n') : (b.data.text ?? '')
          }));
          this.blocks.set(parsed.length > 0 ? parsed : [{ type: 'paragraph', content: '' }]);
        }
      } catch {
        this.blocks.set([{ type: 'paragraph', content: noticia.conteudo }]);
      }
    } catch {
      this.notification.error('Erro ao carregar a notícia.');
      this.router.navigate(['/']);
    } finally {
      this.carregando.set(false);
    }
  }

  // ─── Upload de imagem ──────────────────────────────���────────────────────────

  triggerFileInput(): void {
    this.fileInput.nativeElement.click();
  }

  async onFileSelected(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.carregandoUpload.set(true);
    try {
      const resp = await firstValueFrom(this.uploadService.uploadImagem(file));
      this.form.patchValue({ imagemUrl: resp.url });
      this.imagemPreview.set(resp.url);
    } catch {
      this.notification.error('Erro ao fazer upload da imagem. Use apenas JPG, PNG ou WebP.');
    } finally {
      this.carregandoUpload.set(false);
      input.value = '';
    }
  }

  removerImagem(): void {
    this.form.patchValue({ imagemUrl: '' });
    this.imagemPreview.set(null);
  }

  // ─── IA — título ────────────────────────────────────────────────────────────

  async melhorarTitulo(): Promise<void> {
    const tituloAtual = this.form.value.titulo?.trim();
    if (!tituloAtual) {
      this.notification.warning('Escreva um título antes de pedir para a IA melhorar.');
      return;
    }

    this.carregandoIa.set(true);
    try {
      const resumo = this.blocks()
        .map(b => b.content)
        .filter(c => c.trim())
        .join(' ')
        .substring(0, 300);

      const res = await firstValueFrom(
        this.iaService.melhorarTitulo({ tituloAtual, conteudoResumo: resumo })
      );
      if (res.titulo) {
        this.form.patchValue({ titulo: res.titulo });
        this.notification.success('Título melhorado pela IA!');
      }
    } catch {
      this.notification.error('A IA não conseguiu melhorar o título. Tente novamente.');
    } finally {
      this.carregandoIa.set(false);
    }
  }

  // ─── IA — reescrever artigo completo ────────────────────────────────────────

  async reescreverComoNoticia(): Promise<void> {
    const titulo = this.form.value.titulo?.trim();
    const conteudoTexto = this.blocks()
      .map(b => b.content.trim())
      .filter(c => c)
      .join('\n\n');

    if (!conteudoTexto) {
      this.notification.warning('Escreva algum conteúdo antes de pedir para a IA reescrever.');
      return;
    }

    this.carregandoIa.set(true);
    try {
      const res = await firstValueFrom(
        this.iaService.reescreverNoticia({ titulo: titulo || 'Sem título', conteudo: conteudoTexto })
      );

      if (res.titulo) {
        this.form.patchValue({ titulo: res.titulo });
      }

      if (res.conteudo) {
        const paragrafos = res.conteudo
          .split(/\n\n+/)
          .map(p => p.trim())
          .filter(p => p);

        const novosBlocks: Block[] = paragrafos.map(p => ({ type: 'paragraph', content: p }));
        this.blocks.set(novosBlocks.length > 0 ? novosBlocks : [{ type: 'paragraph', content: res.conteudo }]);

        // Sincroniza o DOM
        setTimeout(() => this.sincronizarBlocsDOM(), 50);
      }

      this.notification.success('Artigo reescrito pela IA!');
    } catch {
      this.notification.error('A IA falhou ao reescrever o artigo. Tente novamente.');
    } finally {
      this.carregandoIa.set(false);
    }
  }

  // ─── IA — bloco individual ──────────────────────────────────────────────────

  async refinarComIA(index: number, estilo: 'formal' | 'criativo' | 'resumido'): Promise<void> {
    const texto = this.blocks()[index].content.replace('/', '').trim();
    if (!texto) {
      this.fecharIaMenu();
      return;
    }

    this.carregandoIa.set(true);
    this.fecharIaMenu();

    try {
      const res = await firstValueFrom(
        this.iaService.refinarConteudo({ textoAtual: texto, instrucao: estilo })
      );
      const updated = [...this.blocks()];
      updated[index] = { ...updated[index], content: res.conteudo };
      this.blocks.set(updated);

      setTimeout(() => {
        const el = this.getBlockEl(index);
        if (el) el.innerText = res.conteudo;
      });
    } catch {
      this.notification.error('A IA falhou ao processar o texto.');
    } finally {
      this.carregandoIa.set(false);
    }
  }

  // ─── Mudar tipo do bloco (formatação via slash command) ─────────────────────

  mudarTipoBloco(index: number, tipo: Block['type']): void {
    const updated = [...this.blocks()];
    const conteudoLimpo = updated[index].content.replace(/^\/\s*/, '').trim();
    updated[index] = { type: tipo, content: conteudoLimpo };
    this.blocks.set(updated);
    this.fecharIaMenu();
    setTimeout(() => {
      const el = this.getBlockEl(index);
      if (el) {
        el.innerText = conteudoLimpo;
        el.focus();
        // posiciona cursor no final
        const range = document.createRange();
        const sel = window.getSelection();
        range.selectNodeContents(el);
        range.collapse(false);
        sel?.removeAllRanges();
        sel?.addRange(range);
      }
    }, 30);
  }

  getBlockClass(block: Block): string {
    return `editable-block block-${block.type}`;
  }

  // ─── Editor — manipulação de blocos ─────────────────────────────────────────

  onBlockKeydown(event: KeyboardEvent, index: number): void {
    // Abre o menu
    if (event.key === '/') {
      event.preventDefault();
      const target = event.target as HTMLElement;
      this.slashMenuIndex.set(0);
      this.slashAnchorEl = target;
      const pos = this.calcularPosicaoMenu(target);
      this.showIaMenu.set({ visible: true, blockIndex: index, ...pos });
      this.registrarScrollListener();
      return;
    }

    // Navegação dentro do menu aberto
    if (this.showIaMenu().visible) {
      if (event.key === 'ArrowDown') {
        event.preventDefault();
        const next = (this.slashMenuIndex() + 1) % this.SLASH_TOTAL;
        this.slashMenuIndex.set(next);
        this.scrollSlashItem(next);
        return;
      }
      if (event.key === 'ArrowUp') {
        event.preventDefault();
        const prev = (this.slashMenuIndex() - 1 + this.SLASH_TOTAL) % this.SLASH_TOTAL;
        this.slashMenuIndex.set(prev);
        this.scrollSlashItem(prev);
        return;
      }
      if (event.key === 'Enter') {
        event.preventDefault();
        this.executarItemSlash(this.showIaMenu().blockIndex, this.slashMenuIndex());
        return;
      }
      if (event.key === 'Escape') {
        event.preventDefault();
        this.fecharIaMenu();
        return;
      }
      // Qualquer outra tecla fecha o menu e continua o fluxo normal
      this.fecharIaMenu();
    }

    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      const newBlocks = [...this.blocks()];
      newBlocks.splice(index + 1, 0, { type: 'paragraph', content: '' });
      this.blocks.set(newBlocks);
      setTimeout(() => this.focusBlock(index + 1), 30);
    } else if (event.key === 'Backspace' && this.blocks()[index].content === '') {
      if (this.blocks()[index].type !== 'paragraph') {
        event.preventDefault();
        this.mudarTipoBloco(index, 'paragraph');
      } else if (this.blocks().length > 1) {
        event.preventDefault();
        const newBlocks = [...this.blocks()];
        newBlocks.splice(index, 1);
        this.blocks.set(newBlocks);
        const destino = index > 0 ? index - 1 : 0;
        setTimeout(() => this.focusBlockNoFinal(destino), 30);
      }
    }
  }

  private executarItemSlash(blockIndex: number, slashIndex: number): void {
    if (slashIndex < this.SLASH_FORMAT_TYPES.length) {
      this.mudarTipoBloco(blockIndex, this.SLASH_FORMAT_TYPES[slashIndex]);
    } else {
      this.refinarComIA(blockIndex, this.SLASH_IA_ESTILOS[slashIndex - this.SLASH_FORMAT_TYPES.length]);
    }
  }

  private scrollSlashItem(index: number): void {
    setTimeout(() => {
      const items = document.querySelectorAll('.slash-list li');
      (items[index] as HTMLElement)?.scrollIntoView({ block: 'nearest' });
    });
  }

  onBlockInput(event: Event, index: number): void {
    // Mutação direta no objeto sem chamar .set() — o signal não notifica o CD,
    // o [innerText] binding não dispara, o cursor não é resetado.
    // O signal permanece em sincronia porque o objeto é o mesmo por referência.
    this.blocks()[index].content = (event.target as HTMLElement).innerText;
  }

  onBlockFocus(index: number): void {
    this.currentBlockFocus.set(index);
  }

  fecharIaMenu(): void {
    if (this.showIaMenu().visible) {
      this.showIaMenu.set({ visible: false, blockIndex: 0, top: 0, left: 0, maxHeight: 380 });
      this.slashMenuIndex.set(0);
      this.removerScrollListener();
      this.slashAnchorEl = null;
    }
  }

  ngAfterViewChecked(): void {
    // Inicializa o innerText apenas para elementos recém-criados pelo @for.
    // Elementos já inicializados (user está digitando neles) são ignorados,
    // evitando o reset do cursor.
    this.blockEls?.forEach((ref, i) => {
      const el = ref.nativeElement as HTMLElement;
      if (!this.initializedBlocks.has(el)) {
        el.innerText = this.blocks()[i]?.content ?? '';
        this.initializedBlocks.add(el);
      }
    });
  }

  ngOnDestroy(): void {
    this.removerScrollListener();
    if (this.rafId !== null) cancelAnimationFrame(this.rafId);
  }

  // ─── Posicionamento inteligente do slash menu ────────────────────────────────

  private calcularPosicaoMenu(el: HTMLElement): { top: number; left: number; maxHeight: number } {
    const rect = el.getBoundingClientRect();
    const GAP = 6;
    const MARGIN = 8;
    const vh = window.innerHeight;
    const vw = window.innerWidth;

    const spaceBelow = vh - rect.bottom - GAP - MARGIN;
    const spaceAbove = rect.top - GAP - MARGIN;
    const openDown = spaceBelow >= 180 || spaceBelow >= spaceAbove;

    let top: number;
    let maxHeight: number;

    if (openDown) {
      top = rect.bottom + GAP;
      maxHeight = Math.min(this.MENU_MAX_HEIGHT, Math.max(150, spaceBelow));
    } else {
      maxHeight = Math.min(this.MENU_MAX_HEIGHT, Math.max(150, spaceAbove));
      top = rect.top - maxHeight - GAP;
    }

    // Clamp horizontal
    let left = rect.left;
    if (left + this.MENU_WIDTH > vw - MARGIN) {
      left = vw - this.MENU_WIDTH - MARGIN;
    }
    left = Math.max(MARGIN, left);

    return { top, left, maxHeight };
  }

  private registrarScrollListener(): void {
    this.removerScrollListener();
    this.slashScrollHandler = () => {
      if (this.rafId !== null) return;
      this.rafId = requestAnimationFrame(() => {
        this.rafId = null;
        if (this.slashAnchorEl && this.showIaMenu().visible) {
          const pos = this.calcularPosicaoMenu(this.slashAnchorEl);
          this.showIaMenu.update(m => ({ ...m, ...pos }));
        }
      });
    };
    // capture: true apanha scroll de qualquer container, não só window
    window.addEventListener('scroll', this.slashScrollHandler, { passive: true, capture: true });
  }

  private removerScrollListener(): void {
    if (this.slashScrollHandler) {
      window.removeEventListener('scroll', this.slashScrollHandler, { capture: true } as EventListenerOptions);
      this.slashScrollHandler = null;
    }
  }

  focusBlock(index: number): void {
    const el = this.getBlockEl(index);
    if (el) el.focus();
  }

  focusBlockNoFinal(index: number): void {
    const el = this.getBlockEl(index);
    if (!el) return;
    el.focus();
    const range = document.createRange();
    const sel = window.getSelection();
    range.selectNodeContents(el);
    range.collapse(false);
    sel?.removeAllRanges();
    sel?.addRange(range);
  }

  private getBlockEl(index: number): HTMLElement | null {
    const els = document.querySelectorAll('.editable-block');
    return (els[index] as HTMLElement) ?? null;
  }

  private sincronizarBlocsDOM(): void {
    // Força re-inicialização: remove do WeakSet para que ngAfterViewChecked
    // reescreva o conteúdo (usada após reescrita por IA).
    this.blockEls?.forEach(ref => {
      const el = ref.nativeElement as HTMLElement;
      this.initializedBlocks.delete(el);
    });
  }

  // ─── Salvar ──────────────────────────────────────────────────────────────────

  async salvar(): Promise<void> {
    if (this.form.invalid) {
      this.notification.warning('Preencha os campos obrigatórios.');
      return;
    }

    this.carregando.set(true);
    const { titulo, categoriaId, imagemUrl } = this.form.value;

    const jsonConteudo = {
      blocks: this.blocks().map(b => ({
        type: b.type,
        data: { text: b.content }
      }))
    };

    const resumo = this.blocks().find(b => b.content.trim())?.content.substring(0, 150) ?? '';

    try {
      if (this.editandoId()) {
        await firstValueFrom(this.noticiaService.atualizar(this.editandoId()!, {
          titulo:    titulo!,
          conteudo:  JSON.stringify(jsonConteudo),
          resumo,
          imagemUrl: imagemUrl ?? null
        }));
        this.notification.success('Notícia atualizada com sucesso!');
      } else {
        await firstValueFrom(this.noticiaService.criar({
          titulo:                titulo!,
          conteudo:              JSON.stringify(jsonConteudo),
          resumo,
          categoriaId:           categoriaId!,
          imagemUrl:             imagemUrl ?? undefined,
          publicarImediatamente: true
        }));
        this.notification.success('Notícia publicada com sucesso!');
      }
      this.router.navigate(['/minhas-noticias']);
    } catch {
      this.notification.error('Erro ao salvar notícia.');
    } finally {
      this.carregando.set(false);
    }
  }
}
