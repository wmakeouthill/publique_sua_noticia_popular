import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NoticiaService } from '../../../services/noticia.service';
import { NotificationService } from '../../../services/notification.service';
import { NoticiaResumo } from '../../../models/noticia.model';
import { TempoRelativoPipe } from '../../../shared/pipes/tempo-relativo.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-minhas-noticias',
  standalone: true,
  imports: [CommonModule, RouterModule, TempoRelativoPipe],
  templateUrl: './minhas-noticias.component.html',
  styleUrl: './minhas-noticias.component.css'
})
export class MinhasNoticiasComponent implements OnInit {
  private readonly noticiaService = inject(NoticiaService);
  private readonly notification = inject(NotificationService);

  readonly noticias = signal<NoticiaResumo[]>([]);
  readonly carregando = signal(false);
  readonly pagina = signal(0);
  readonly ultimaPagina = signal(false);
  readonly excluindo = signal<string | null>(null);

  ngOnInit(): void {
    this.carregar();
  }

  async carregar(novaBusca = true): Promise<void> {
    if (novaBusca) {
      this.pagina.set(0);
      this.noticias.set([]);
    }
    this.carregando.set(true);
    try {
      const page = await firstValueFrom(
        this.noticiaService.minhasNoticias(this.pagina(), 12)
      );
      this.noticias.update(lista => novaBusca ? page.content : [...lista, ...page.content]);
      this.ultimaPagina.set(page.last);
    } catch {
      this.notification.error('Falha ao carregar suas notícias.');
    } finally {
      this.carregando.set(false);
    }
  }

  carregarMais(): void {
    if (!this.ultimaPagina() && !this.carregando()) {
      this.pagina.update(p => p + 1);
      this.carregar(false);
    }
  }

  async excluir(id: string, titulo: string): Promise<void> {
    if (!confirm(`Excluir "${titulo}"? Esta ação não pode ser desfeita.`)) return;
    this.excluindo.set(id);
    try {
      await firstValueFrom(this.noticiaService.excluir(id));
      this.noticias.update(lista => lista.filter(n => n.id !== id));
      this.notification.success('Notícia excluída com sucesso.');
    } catch {
      this.notification.error('Erro ao excluir a notícia.');
    } finally {
      this.excluindo.set(null);
    }
  }

  statusLabel(status: string): string {
    const map: Record<string, string> = {
      PUBLICADA: 'Publicada', RASCUNHO: 'Rascunho', ARQUIVADA: 'Arquivada'
    };
    return map[status] ?? status;
  }
}
