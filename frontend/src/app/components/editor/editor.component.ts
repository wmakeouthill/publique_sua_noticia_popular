import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NoticiaService } from '../../services/noticia.service';
import { IaService } from '../../services/ia.service';
import { CategoriaService } from '../../services/categoria.service';
import { NotificationService } from '../../services/notification.service';
import { firstValueFrom } from 'rxjs';
import { Categoria } from '../../models/categoria.model';

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

  readonly editandoId = signal<string | null>(null);
  readonly carregando = signal(false);
  readonly carregandoIa = signal(false);
  readonly categorias = signal<Categoria[]>([]);
  readonly currentBlockFocus = signal<number>(0);

  readonly showIaMenu = signal<{ visible: boolean, blockIndex: number, top: number, left: number }>({
    visible: false, blockIndex: 0, top: 0, left: 0
  });

  form = this.fb.group({
    titulo:     ['', [Validators.required, Validators.minLength(5)]],
    categoriaId:['', Validators.required],
    imagemUrl:  ['']
  });

  blocks = signal<{ type: string, content: string }[]>([
    { type: 'paragraph', content: '' }
  ]);

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
    } catch { /* ignora erro de carregamento de categorias */ }
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

      try {
        const conteudoJson = JSON.parse(noticia.conteudo);
        if (conteudoJson?.blocks) {
          const mapBlocks = conteudoJson.blocks.map((b: { type: string; data: { items?: string[]; text?: string } }) => ({
            type:    b.type,
            content: b.type === 'list' ? b.data.items!.join('\n') : (b.data.text ?? '')
          }));
          this.blocks.set(mapBlocks.length > 0 ? mapBlocks : [{ type: 'paragraph', content: '' }]);
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

  onBlockKeydown(event: KeyboardEvent, index: number): void {
    if (event.key === '/') {
      const target = event.target as HTMLElement;
      const rect = target.getBoundingClientRect();
      this.showIaMenu.set({
        visible: true, blockIndex: index,
        top: rect.bottom + window.scrollY, left: rect.left + window.scrollX
      });
    } else if (event.key === 'Enter' && !event.shiftKey && !this.showIaMenu().visible) {
      event.preventDefault();
      const newBlocks = [...this.blocks()];
      newBlocks.splice(index + 1, 0, { type: 'paragraph', content: '' });
      this.blocks.set(newBlocks);
      setTimeout(() => this.focusBlock(index + 1), 50);
    } else if (event.key === 'Backspace' && this.blocks()[index].content === '') {
      if (this.blocks().length > 1) {
        event.preventDefault();
        const newBlocks = [...this.blocks()];
        newBlocks.splice(index, 1);
        this.blocks.set(newBlocks);
        this.focusBlock(index > 0 ? index - 1 : 0);
      }
    } else {
      this.showIaMenu.set({ visible: false, blockIndex: 0, top: 0, left: 0 });
    }
  }

  onBlockInput(event: Event, index: number): void {
    const target = event.target as HTMLElement;
    const blocks = [...this.blocks()];
    blocks[index] = { ...blocks[index], content: target.innerText };
    this.blocks.set(blocks);
  }

  onBlockFocus(index: number): void {
    this.currentBlockFocus.set(index);
  }

  focusBlock(index: number): void {
    const els = document.querySelectorAll('.editable-block');
    if (els[index]) (els[index] as HTMLElement).focus();
  }

  async refinarComIA(index: number, estilo: 'formal' | 'criativo' | 'resumido'): Promise<void> {
    const texto = this.blocks()[index].content.replace('/', '').trim();
    if (!texto) {
      this.showIaMenu.set({ visible: false, blockIndex: 0, top: 0, left: 0 });
      return;
    }

    this.carregandoIa.set(true);
    this.showIaMenu.set({ visible: false, blockIndex: 0, top: 0, left: 0 });

    try {
      const res = await firstValueFrom(
        this.iaService.refinarConteudo({ textoAtual: texto, instrucao: estilo })
      );
      const updated = [...this.blocks()];
      updated[index] = { ...updated[index], content: res.conteudo };
      this.blocks.set(updated);
      setTimeout(() => {
        const els = document.querySelectorAll('.editable-block');
        if (els[index]) (els[index] as HTMLElement).innerText = res.conteudo;
      });
    } catch {
      this.notification.error('A IA falhou ao processar o texto.');
    } finally {
      this.carregandoIa.set(false);
    }
  }

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
        data: b.type === 'list' ? { items: b.content.split('\n') } : { text: b.content }
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
