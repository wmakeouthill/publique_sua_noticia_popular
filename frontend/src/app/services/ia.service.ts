import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { MelhorarTituloRequest, PromptRefinarTexto, ReescreverNoticiaRequest, RespostaIA } from '../models/ia.model';

@Injectable({
  providedIn: 'root'
})
export class IaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/ia`;

  gerarTextoDaNoticia(prompt: string, categoriaHint?: string): Observable<RespostaIA> {
    return this.http.post<RespostaIA>(`${this.baseUrl}/gerar-texto`, { prompt, categoriaHint });
  }

  gerarImagemParaNoticia(prompt: string): Observable<{imageUrl: string, prompt: string}> {
    return this.http.post<{imageUrl: string, prompt: string}>(`${this.baseUrl}/gerar-imagem`, { prompt });
  }

  refinarConteudo(request: PromptRefinarTexto): Observable<RespostaIA> {
    return this.http.post<RespostaIA>(`${this.baseUrl}/refinar-texto`, request);
  }

  reescreverNoticia(request: ReescreverNoticiaRequest): Observable<RespostaIA> {
    return this.http.post<RespostaIA>(`${this.baseUrl}/reescrever-noticia`, request);
  }

  melhorarTitulo(request: MelhorarTituloRequest): Observable<RespostaIA> {
    return this.http.post<RespostaIA>(`${this.baseUrl}/melhorar-titulo`, request);
  }
}
