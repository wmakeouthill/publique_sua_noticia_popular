import { inject, Injectable, signal, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { AuthResponse, PerfilUsuario } from '../models/usuario.model';
import { environment } from '../../environments/environment';
import { firstValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);
  // Não injetando Router aqui para evitar conflitos, podemos emitir erro e deixar app resolver

  private readonly TOKEN_KEY = 'pop_auth_token';
  private readonly REFRESH_TOKEN_KEY = 'pop_refresh_token';
  private readonly PERFIL_KEY = 'pop_auth_profile';

  // Signals para estado
  readonly usuarioAtual = signal<PerfilUsuario | null>(null);
  readonly autenticado = signal<boolean>(false);
  readonly carregando = signal<boolean>(true);

  constructor() {
    if (this.isBrowser) {
      this.iniciarSessao();
    } else {
      this.carregando.set(false);
    }
  }

  private async iniciarSessao(): Promise<void> {
    const token = this.obterToken();
    if (!token) {
      this.limparEstadoEmMemoria();
      this.carregando.set(false);
      return;
    }

    if (this.tokenExpirado(token)) {
      this.limparSessao();
      this.carregando.set(false);
      return;
    }

    // Restaura do cache e libera o app imediatamente
    this.restaurarPerfilCache();
    this.carregando.set(false);

    // Valida com o servidor em background (sem bloquear a UI)
    try {
      const perfil = await firstValueFrom(
        this.http.get<PerfilUsuario>(`${environment.apiUrl}/auth/perfil`)
      );
      this.definirSessaoAutenticada(perfil);
    } catch (e) {
      if (this.deveInvalidarSessao(e)) {
        this.limparSessao();
      }
    }
  }

  async loginComGoogle(credential: string): Promise<boolean> {
    try {
      const res = await firstValueFrom(
        this.http.post<AuthResponse>(`${environment.apiUrl}/auth/google`, { googleToken: credential })
      );
      this.salvarSessao(res.accessToken, res.refreshToken);

      // Busca o perfil completo após o login
      const perfil = await firstValueFrom(
        this.http.get<PerfilUsuario>(`${environment.apiUrl}/auth/perfil`)
      );
      this.definirSessaoAutenticada(perfil);
      return true;
    } catch {
      return false;
    }
  }

  async refreshToken(): Promise<boolean> {
    const refreshToken = this.isBrowser ? localStorage.getItem(this.REFRESH_TOKEN_KEY) : null;
    if (!refreshToken) return false;

    try {
      const res = await firstValueFrom(
        this.http.post<AuthResponse>(`${environment.apiUrl}/auth/refresh`, { refreshToken })
      );
      this.salvarSessao(res.accessToken, res.refreshToken);
      return true;
    } catch {
      this.limparSessao();
      return false;
    }
  }

  logout(): void {
    this.limparSessao();
  }

  obterToken(): string | null {
    if (!this.isBrowser) return null;
    return localStorage.getItem(this.TOKEN_KEY);
  }

  private salvarSessao(token: string, refreshToken: string): void {
    if (!this.isBrowser) return;
    localStorage.setItem(this.TOKEN_KEY, token);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
  }

  private definirSessaoAutenticada(perfil: PerfilUsuario): void {
    this.usuarioAtual.set(perfil);
    this.autenticado.set(true);

    if (this.isBrowser) {
      localStorage.setItem(this.PERFIL_KEY, JSON.stringify(perfil));
    }
  }

  private restaurarPerfilCache(): void {
    if (!this.isBrowser) return;

    const perfilJson = localStorage.getItem(this.PERFIL_KEY);
    if (!perfilJson) return;

    try {
      const perfil = JSON.parse(perfilJson) as PerfilUsuario;
      if (perfil?.id && perfil?.email) {
        this.usuarioAtual.set(perfil);
        this.autenticado.set(true);
      }
    } catch {
      localStorage.removeItem(this.PERFIL_KEY);
    }
  }

  private tokenExpirado(token: string): boolean {
    try {
      const partes = token.split('.');
      if (partes.length !== 3) return true;

      const payloadBase64 = partes[1].replace(/-/g, '+').replace(/_/g, '/');
      const payloadNormalizado = payloadBase64.padEnd(Math.ceil(payloadBase64.length / 4) * 4, '=');
      const payload = JSON.parse(atob(payloadNormalizado)) as { exp?: number };

      if (!payload.exp) return true;
      return payload.exp * 1000 <= Date.now();
    } catch {
      return true;
    }
  }

  private deveInvalidarSessao(error: unknown): boolean {
    if (!(error instanceof HttpErrorResponse)) {
      return false;
    }

    return error.status === 401 || error.status === 403;
  }

  private limparEstadoEmMemoria(): void {
    this.usuarioAtual.set(null);
    this.autenticado.set(false);
  }

  private limparSessao(): void {
    if (this.isBrowser) {
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.REFRESH_TOKEN_KEY);
      localStorage.removeItem(this.PERFIL_KEY);
    }
    this.limparEstadoEmMemoria();
  }
}
