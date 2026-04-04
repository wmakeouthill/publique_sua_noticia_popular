import { Component, inject, signal, OnInit, ViewChild, ElementRef } from '@angular/core';
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
  type: 'paragraph' | 'header';
  content: string;
}

@Component({
  selector: 'app-editor',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './editor.component.html',
  styleUrl: './editor.component.css'
})
export class EditorComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly noticiaService = inject(NoticiaService);
  private readonly iaService = inject(IaService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly notification = inject(NotificationService);
  private readonly uploadService = inject(UploadService);

  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  readonly editandoId = signal<string | null>(null);
  readonly carregando = signal(false);
  readonly carregandoIa = signal(false);
  readonly carregandoUpload = signal(false);
  readonly categorias = signal<Categoria[]>([]);
  readonly currentBlockFocus = signal(0);
  readonly imagemPreview = signal<string | null>(null);

  readonly showIaMenu = signal<{ visible: boolean; blockIndex: number; top: number; left: number }>({
    visible: false, blockIndex: 0, top: 0, left: 0
  });

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

  // ─── Editor — manipulação de blocos ─────────────────────────────────────────

  onBlockKeydown(event: KeyboardEvent, index: number): void {
    if (event.key === '/') {
      event.preventDefault();
      const target = event.target as HTMLElement;
      const rect = target.getBoundingClientRect();
      this.showIaMenu.set({
        visible: true,
        blockIndex: index,
        top: rect.bottom + 6,
        left: rect.left
      });
      return;
    }

    this.fecharIaMenu();

    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      const newBlocks = [...this.blocks()];
      newBlocks.splice(index + 1, 0, { type: 'paragraph', content: '' });
      this.blocks.set(newBlocks);
      setTimeout(() => this.focusBlock(index + 1), 30);
    } else if (event.key === 'Backspace' && this.blocks()[index].content === '') {
      if (this.blocks().length > 1) {
        event.preventDefault();
        const newBlocks = [...this.blocks()];
        newBlocks.splice(index, 1);
        this.blocks.set(newBlocks);
        this.focusBlock(index > 0 ? index - 1 : 0);
      }
    }
  }

  onBlockInput(event: Event, index: number): void {
    const target = event.target as HTMLElement;
    const current = [...this.blocks()];
    current[index] = { ...current[index], content: target.innerText };
    this.blocks.set(current);
  }

  onBlockFocus(index: number): void {
    this.currentBlockFocus.set(index);
  }

  fecharIaMenu(): void {
    if (this.showIaMenu().visible) {
      this.showIaMenu.set({ visible: false, blockIndex: 0, top: 0, left: 0 });
    }
  }

  focusBlock(index: number): void {
    const el = this.getBlockEl(index);
    if (el) el.focus();
  }

  private getBlockEl(index: number): HTMLElement | null {
    const els = document.querySelectorAll('.editable-block');
    return (els[index] as HTMLElement) ?? null;
  }

  private sincronizarBlocsDOM(): void {
    const els = document.querySelectorAll('.editable-block');
    this.blocks().forEach((block, i) => {
      const el = els[i] as HTMLElement;
      if (el && el.innerText !== block.content) {
        el.innerText = block.content;
      }
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
