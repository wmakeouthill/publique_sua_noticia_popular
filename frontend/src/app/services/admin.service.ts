import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PerfilUsuario } from '../models/usuario.model';

export interface AdminDashboard {
  totalUsuarios: number;
  totalNoticias: number;
  noticiasPublicadas: number;
  noticiasRascunho: number;
  totalCategorias: number;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin`;

  dashboard(): Observable<AdminDashboard> {
    return this.http.get<AdminDashboard>(`${this.baseUrl}/dashboard`);
  }

  listarUsuarios(): Observable<PerfilUsuario[]> {
    return this.http.get<PerfilUsuario[]>(`${this.baseUrl}/usuarios`);
  }

  alterarStatusUsuario(id: string, ativo: boolean): Observable<PerfilUsuario> {
    return this.http.patch<PerfilUsuario>(`${this.baseUrl}/usuarios/${id}`, { ativo });
  }
}
