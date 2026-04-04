import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Comentario, CriarComentarioRequest } from '../models/comentario.model';

@Injectable({
  providedIn: 'root'
})
export class ComentarioService {
  private readonly http = inject(HttpClient);

  private baseUrl(noticiaId: string): string {
    return `${environment.apiUrl}/noticias/${noticiaId}/comentarios`;
  }

  listar(noticiaId: string, ordenacao = 'MAIS_RECENTE'): Observable<Comentario[]> {
    return this.http.get<Comentario[]>(this.baseUrl(noticiaId), {
      params: { ordenacao }
    });
  }

  criar(noticiaId: string, request: CriarComentarioRequest): Observable<Comentario> {
    return this.http.post<Comentario>(this.baseUrl(noticiaId), request);
  }

  excluir(noticiaId: string, comentarioId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl(noticiaId)}/${comentarioId}`);
  }
}
