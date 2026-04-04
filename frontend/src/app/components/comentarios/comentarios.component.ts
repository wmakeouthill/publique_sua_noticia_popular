import { Component, inject, input, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ComentarioService } from '../../services/comentario.service';
import { ReacaoService } from '../../services/reacao.service';
import { AuthService } from '../../services/auth.service';
import { Comentario } from '../../models/comentario.model';
import { TempoRelativoPipe } from '../../shared/pipes/tempo-relativo.pipe';
import { firstValueFrom } from 'rxjs';

type OrdenacaoComentario = 'MAIS_RECENTE' | 'MAIS_ANTIGO' | 'MAIS_CURTIDO';

@Component({
  selector: 'app-comentarios',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, TempoRelativoPipe],
  templateUrl: './comentarios.component.html',
  styleUrl: './comentarios.component.css'
})
export class ComentariosComponent implements OnInit {
  private readonly comentarioService = inject(ComentarioService);
  private readonly reacaoService = inject(ReacaoService);
  readonly authService = inject(AuthService);

  readonly noticiaId = input.required<string>();

  readonly comentarios = signal<Comentario[]>([]);
  readonly carregando = signal(false);
  readonly enviando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly novoComentario = signal('');
  readonly erroEnvio = signal<string | null>(null);
  readonly ordenacao = signal<OrdenacaoComentario>('MAIS_RECENTE');
  readonly sortDropdownOpen = signal(false);
  readonly togglendoLike = signal<Set<string>>(new Set());

  readonly total = computed(() => this.comentarios().length);

  readonly ordenacaoOpcoes: { valor: OrdenacaoComentario; label: string }[] = [
    { valor: 'MAIS_RECENTE', label: 'Mais recentes' },
    { valor: 'MAIS_ANTIGO',  label: 'Mais antigos' },
    { valor: 'MAIS_CURTIDO', label: 'Mais curtidos' },
  ];

  readonly ordenacaoLabel = computed(() =>
    this.ordenacaoOpcoes.find(o => o.valor === this.ordenacao())?.label ?? 'Ordenar'
  );

  ngOnInit(): void {
    this.carregarComentarios();
  }

  async carregarComentarios(): Promise<void> {
    this.carregando.set(true);
    this.erro.set(null);
    try {
      const lista = await firstValueFrom(
        this.comentarioService.listar(this.noticiaId(), this.ordenacao())
      );
      this.comentarios.set(lista);
    } catch {
      this.erro.set('Não foi possível carregar os comentários.');
    } finally {
      this.carregando.set(false);
    }
  }

  async enviarComentario(): Promise<void> {
    const conteudo = this.novoComentario().trim();
    if (!conteudo) return;
    if (conteudo.length > 1000) {
      this.erroEnvio.set('Comentário deve ter no máximo 1000 caracteres.');
      return;
    }

    this.enviando.set(true);
    this.erroEnvio.set(null);
    try {
      const criado = await firstValueFrom(
        this.comentarioService.criar(this.noticiaId(), { conteudo })
      );
      this.comentarios.update(lista => [criado, ...lista]);
      this.novoComentario.set('');
    } catch {
      this.erroEnvio.set('Erro ao enviar comentário. Tente novamente.');
    } finally {
      this.enviando.set(false);
    }
  }

  async excluirComentario(comentarioId: string): Promise<void> {
    try {
      await firstValueFrom(this.comentarioService.excluir(this.noticiaId(), comentarioId));
      this.comentarios.update(lista => lista.filter(c => c.id !== comentarioId));
    } catch {
      // falha silenciosa
    }
  }

  async toggleLike(comentario: Comentario): Promise<void> {
    if (!this.authService.autenticado()) return;
    const id = comentario.id;
    if (this.togglendoLike().has(id)) return;

    this.togglendoLike.update(s => { const n = new Set(s); n.add(id); return n; });
    try {
      const status = await firstValueFrom(
        this.reacaoService.toggle({ alvoTipo: 'COMENTARIO', alvoId: id })
      );
      this.comentarios.update(lista =>
        lista.map(c => c.id === id
          ? { ...c, totalLikes: status.total, likedByMe: status.likedByMe }
          : c
        )
      );
    } finally {
      this.togglendoLike.update(s => { const n = new Set(s); n.delete(id); return n; });
    }
  }

  mudarOrdenacao(ord: OrdenacaoComentario): void {
    this.ordenacao.set(ord);
    this.sortDropdownOpen.set(false);
    this.carregarComentarios();
  }

  podeExcluir(comentario: Comentario): boolean {
    const usuario = this.authService.usuarioAtual();
    if (!usuario) return false;
    return usuario.id === comentario.autorId || usuario.papel === 'ADMIN';
  }

  onTextoChange(event: Event): void {
    const target = event.target as HTMLTextAreaElement;
    this.novoComentario.set(target.value);
  }
}
