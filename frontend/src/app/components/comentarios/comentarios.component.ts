import { Component, inject, input, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ComentarioService } from '../../services/comentario.service';
import { AuthService } from '../../services/auth.service';
import { Comentario } from '../../models/comentario.model';
import { TempoRelativoPipe } from '../../shared/pipes/tempo-relativo.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-comentarios',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, TempoRelativoPipe],
  templateUrl: './comentarios.component.html',
  styleUrl: './comentarios.component.css'
})
export class ComentariosComponent implements OnInit {
  private readonly comentarioService = inject(ComentarioService);
  readonly authService = inject(AuthService);

  readonly noticiaId = input.required<string>();

  readonly comentarios = signal<Comentario[]>([]);
  readonly carregando = signal(false);
  readonly enviando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly novoComentario = signal('');
  readonly erroEnvio = signal<string | null>(null);

  readonly total = computed(() => this.comentarios().length);

  ngOnInit(): void {
    this.carregarComentarios();
  }

  async carregarComentarios(): Promise<void> {
    this.carregando.set(true);
    this.erro.set(null);
    try {
      const lista = await firstValueFrom(this.comentarioService.listar(this.noticiaId()));
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
      this.comentarios.update(lista => [...lista, criado]);
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
      // falha silenciosa — o item permanece na lista
    }
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
