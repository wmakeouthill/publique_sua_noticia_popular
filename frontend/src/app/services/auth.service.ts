import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/common/http';
import { AuthResponse, PerfilUsuario } from '../models/usuario.model';
import { environment } from '../../environments/environment';
import { firstValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  // Não injetando Router aqui para evitar conflitos, podemos emitir erro e deixar app resolver

  private readonly TOKEN_KEY = 'pop_auth_token';
  private readonly REFRESH_TOKEN_KEY = 'pop_refresh_token';

  // Signals para estado
  readonly usuarioAtual = signal<PerfilUsuario | null>(null);
  readonly autenticado = signal<boolean>(false);
  readonly carregando = signal<boolean>(true);

  constructor() {
    this.iniciarSessao();
  }

  private async iniciarSessao(): Promise<void> {
    const token = this.obterToken();
    if (!token) {
      this.carregando.set(false);
      return;
    }

    try {
      const perfil = await firstValueFrom(
        this.http.get<PerfilUsuario>(`${environment.apiUrl}/auth/perfil`)
      );
      this.usuarioAtual.set(perfil);
      this.autenticado.set(true);
    } catch (e) {
      this.limparSessao();
    } finally {
      this.carregando.set(false);
    }
  }

  async loginComGoogle(credential: string): Promise<boolean> {
    try {
      this.carregando.set(true);
      const res = await firstValueFrom(
        this.http.post<AuthResponse>(`${environment.apiUrl}/auth/google`, { idToken: credential })
      );

      this.salvarSessao(res.token, res.refreshToken);
      await this.iniciarSessao();
      return true;
    } catch (e) {
      this.carregando.set(false);
      return false;
    }
  }

  logout(): void {
    this.limparSessao();
  }

  obterToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  private salvarSessao(token: string, refreshToken: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
  }

  private limparSessao(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    this.usuarioAtual.set(null);
    this.autenticado.set(false);
  }
}
