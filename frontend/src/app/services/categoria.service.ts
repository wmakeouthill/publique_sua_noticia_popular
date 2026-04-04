import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Categoria } from '../models/categoria.model';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';

export interface CriarCategoriaRequest {
  nome: string;
  descricao?: string;
  icone?: string;
}

export interface AtualizarCategoriaRequest {
  nome?: string;
  descricao?: string;
  icone?: string;
  ordem?: number;
}

@Injectable({
  providedIn: 'root'
})
export class CategoriaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/categorias`;

  buscarCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.baseUrl);
  }

  criar(request: CriarCategoriaRequest): Observable<Categoria> {
    return this.http.post<Categoria>(this.baseUrl, request);
  }

  atualizar(id: string, request: AtualizarCategoriaRequest): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.baseUrl}/${id}`, request);
  }

  desativar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
