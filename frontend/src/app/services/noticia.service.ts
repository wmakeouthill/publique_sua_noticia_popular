import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CriarNoticiaRequest, Noticia, NoticiaResumo } from '../models/noticia.model';
import { Pagina } from '../models/paginacao.model';

@Injectable({
  providedIn: 'root'
})
export class NoticiaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/noticias`;

  feed(pagina = 0, tamanho = 12, categoriaId?: string, busca?: string): Observable<Pagina<NoticiaResumo>> {
    let params = new HttpParams()
      .set('pagina', pagina)
      .set('tamanho', tamanho);

    if (categoriaId) params = params.set('categoriaId', categoriaId);
    if (busca) params = params.set('busca', busca);

    return this.http.get<Pagina<NoticiaResumo>>(this.baseUrl, { params });
  }

  minhasNoticias(pagina = 0, tamanho = 12): Observable<Pagina<NoticiaResumo>> {
    const params = new HttpParams()
      .set('pagina', pagina)
      .set('tamanho', tamanho);
      
    return this.http.get<Pagina<NoticiaResumo>>(`${this.baseUrl}/minhas`, { params });
  }

  buscarPorId(id: string): Observable<Noticia> {
    return this.http.get<Noticia>(`${this.baseUrl}/${id}`);
  }

  criar(request: CriarNoticiaRequest): Observable<Noticia> {
    return this.http.post<Noticia>(this.baseUrl, request);
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
