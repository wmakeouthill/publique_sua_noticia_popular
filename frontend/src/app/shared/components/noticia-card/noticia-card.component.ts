import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NoticiaResumo } from '../../../models/noticia.model';

@Component({
  selector: 'app-noticia-card',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './noticia-card.component.html',
  styleUrl: './noticia-card.component.css'
})
export class NoticiaCardComponent {
  readonly noticia = input.required<NoticiaResumo>();
  readonly onCategoriaClick = output<string>();

  get statusText(): string {
    return this.noticia().status === 'PUBLICADA' ? 'Publicada' 
         : this.noticia().status === 'RASCUNHO' ? 'Rascunho' 
         : 'Arquivada';
  }

  clicarCategoria(event: Event): void {
    event.stopPropagation();
    event.preventDefault();
    this.onCategoriaClick.emit(this.noticia().categoriaId);
  }
}
