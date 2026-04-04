import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ReacaoStatus, ToggleReacaoRequest } from '../models/reacao.model';

@Injectable({
  providedIn: 'root'
})
export class ReacaoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/reacoes`;

  toggle(request: ToggleReacaoRequest): Observable<ReacaoStatus> {
    return this.http.post<ReacaoStatus>(`${this.baseUrl}/toggle`, request);
  }
}
