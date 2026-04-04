import { Component, input, output, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NoticiaResumo } from '../../../models/noticia.model';
import { Categoria } from '../../../models/categoria.model';
import { TempoRelativoPipe } from '../../pipes/tempo-relativo.pipe';

@Component({
  selector: 'app-noticia-card',
  standalone: true,
  imports: [CommonModule, RouterModule, TempoRelativoPipe],
  templateUrl: './noticia-card.component.html',
  styleUrl: './noticia-card.component.css'
})
export class NoticiaCardComponent {
  readonly noticia = input.required<NoticiaResumo>();
  readonly categoriasMap = input<Map<string, Categoria>>(new Map());
  readonly onCategoriaClick = output<string>();

  readonly categoria = computed(() =>
    this.categoriasMap().get(this.noticia().categoriaId)
  );

  get statusText(): string {
    return this.noticia().status === 'PUBLICADA' ? 'Publicada'
         : this.noticia().status === 'RASCUNHO'  ? 'Rascunho'
         : 'Arquivada';
  }

  clicarCategoria(event: Event): void {
    event.stopPropagation();
    event.preventDefault();
    this.onCategoriaClick.emit(this.noticia().categoriaId);
  }
}
