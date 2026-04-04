import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface UploadResponse {
  url: string;
  nomeArquivo: string;
}

@Injectable({
  providedIn: 'root'
})
export class UploadService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/upload`;

  uploadImagem(arquivo: File): Observable<UploadResponse> {
    const formData = new FormData();
    formData.append('arquivo', arquivo);
    return this.http.post<UploadResponse>(`${this.baseUrl}/imagem`, formData);
  }
}
