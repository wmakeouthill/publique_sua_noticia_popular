import { Component, inject, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NoticiaService } from '../../services/noticia.service';
import { IaService } from '../../services/ia.service';
import { CategoriaService } from '../../services/categoria.service';
import { firstValueFrom } from 'rxjs';
import { Categoria } from '../../models/categoria.model';

@Component({
  selector: 'app-editor',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './editor.component.html',
  styleUrl: './editor.component.css'
})
export class EditorComponent {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly noticiaService = inject(NoticiaService);
  private readonly iaService = inject(IaService);
  private readonly categoriaService = inject(CategoriaService);

  readonly editandoId = signal<string | null>(null);
  readonly carregando = signal(false);
  readonly carregandoIa = signal(false);
  readonly categorias = signal<Categoria[]>([]);
  readonly currentBlockFocus = signal<number>(0);

  // Menu de IA Slash Commands Toggle
  readonly showIaMenu = signal<{ visible: boolean, blockIndex: number, top: number, left: number }>({
    visible: false, blockIndex: 0, top: 0, left: 0
  });

  // O Angular form lida com Titulo, Slug e Imagem principal
  form = this.fb.group({
    titulo: ['', [Validators.required, Validators.minLength(5)]],
    categoriaId: ['', Validators.required],
    imagemUrl: ['']
  });

  // Blocos interativos no estilo "Notion"
  blocks = signal<{ type: string, content: string }[]>([
    { type: 'paragraph', content: '' }
  ]);

  constructor() {
    this.carregarCategorias();

    effect(() => {
      const paramId = this.route.snapshot.paramMap.get('id');
      if (paramId) {
        this.editandoId.set(paramId);
        this.carregarNoticia(paramId);
      }
    });
  }

  async carregarCategorias() {
    try {
      const respCategorias = await firstValueFrom(this.categoriaService.buscarCategorias());
      this.categorias.set(respCategorias);
      // setar o 1o id padrao se houver
      if (respCategorias.length > 0) {
        this.form.patchValue({ categoriaId: respCategorias[0].id });
      }
    } catch (e) { }
  }

  async carregarNoticia(id: string) {
    this.carregando.set(true);
    try {
      const noticia = await firstValueFrom(this.noticiaService.buscarPorId(id));
      this.form.patchValue({
        titulo: noticia.titulo,
        categoriaId: noticia.categoriaId,
        imagemUrl: noticia.imagemUrl
      });

      try {
        const conteudoJson = JSON.parse(noticia.conteudo);
        if (conteudoJson && conteudoJson.blocks) {
          const mapBlocks = conteudoJson.blocks.map((b: any) => ({
            type: b.type,
            content: b.type === 'list'
              ? b.data.items.join('\n')
              : b.data.text
          }));
          this.blocks.set(mapBlocks.length > 0 ? mapBlocks : [{ type: 'paragraph', content: '' }]);
        }
      } catch (e) {
        this.blocks.set([{ type: 'paragraph', content: noticia.conteudo }]);
      }
    } catch (e) {
      alert('Erro ao carregar a notícia.');
      this.router.navigate(['/']);
    } finally {
      this.carregando.set(false);
    }
  }

  onBlockKeydown(event: KeyboardEvent, index: number) {
    // Detecta Slash '/' para abrir menu IA
    if (event.key === '/') {
      const target = event.target as HTMLElement;
      const rect = target.getBoundingClientRect();
      this.showIaMenu.set({
        visible: true,
        blockIndex: index,
        top: rect.bottom + window.scrollY,
        left: rect.left + window.scrollX
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
        this.focusBlock(index - 1 > 0 ? index - 1 : 0);
      }
    } else {
      this.showIaMenu.set({ visible: false, blockIndex: 0, top: 0, left: 0 });
    }
  }

  onBlockInput(event: Event, index: number) {
    const target = event.target as HTMLElement;
    const blocks = [...this.blocks()];
    blocks[index].content = target.innerText;
    this.blocks.set(blocks);
  }

  onBlockFocus(index: number) {
    this.currentBlockFocus.set(index);
  }

  focusBlock(index: number) {
    const els = document.querySelectorAll('.editable-block');
    if (els[index]) {
      (els[index] as HTMLElement).focus();
    }
  }

  async refinarComIA(index: number, estilo: 'formal' | 'criativo' | 'resumido') {
    const textToRefine = this.blocks()[index].content.replace('/', '');
    if (!textToRefine) {
      this.showIaMenu.set({ visible: false, blockIndex: 0, top: 0, left: 0 });
      return;
    }

    // Mostra loading localmente enviando req para refinar texto com gemini (via backend)
    this.carregandoIa.set(true);
    this.showIaMenu.set({ visible: false, blockIndex: 0, top: 0, left: 0 });

    try {
      const res = await firstValueFrom(this.iaService.refinarConteudo({
        textoAtual: textToRefine,
        instrucao: estilo
      }));

      const updatedBlocks = [...this.blocks()];
      updatedBlocks[index].content = res.conteudo;
      this.blocks.set(updatedBlocks);

      // Update the DOM manually because contenteditable doesn't bind automatically
      setTimeout(() => {
        const els = document.querySelectorAll('.editable-block');
        if (els[index]) {
          (els[index] as HTMLElement).innerText = res.conteudo;
        }
      });
    } catch (e) {
      alert('A IA falhou ao processar o texto.');
    } finally {
      this.carregandoIa.set(false);
    }
  }

  async salvar() {
    if (this.form.invalid) {
      alert('Preencha os campos obrigatórios primeiro.');
      return;
    }

    this.carregando.set(true);

    const titulo = this.form.value.titulo!;

    // Gerar um JSON estruturado para simular um editor de blocos no backend
    const jsonToSave = {
      blocks: this.blocks().map(b => ({
        type: b.type,
        data: b.type === 'list' ? { items: b.content.split('\n') } : { text: b.content }
      }))
    };

    const payloadCriar = {
      titulo,
      conteudo: JSON.stringify(jsonToSave),
      resumo: this.blocks()[0].content.substring(0, 150) + '...',
      categoriaId: this.form.value.categoriaId!,
      imagemUrl: this.form.value.imagemUrl! || undefined,
      publicarImediatamente: true
    };

    const payloadEditar = {
      titulo,
      conteudo: JSON.stringify(jsonToSave),
      resumo: this.blocks()[0].content.substring(0, 150) + '...',
      imagemUrl: this.form.value.imagemUrl! || null
    };

    try {
      if (this.editandoId()) {
        await firstValueFrom(this.noticiaService.atualizar(this.editandoId()!, payloadEditar));
        alert('Notícia atualizada com sucesso!');
      } else {
        await firstValueFrom(this.noticiaService.criar(payloadCriar));
        alert('Notícia publicada com sucesso!');
      }
      this.router.navigate(['/']);
    } catch (e) {
      alert('Erro ao salvar notícia.');
    } finally {
      this.carregando.set(false);
    }
  }
}
