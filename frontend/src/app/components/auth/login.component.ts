import { Component, inject, AfterViewInit, ElementRef, ViewChild, PLATFORM_ID, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { environment } from '../../../environments/environment';

declare global {
  interface Window {
    google: any;
    __npGsiState?: {
      initialized: boolean;
      clientId?: string;
    };
  }
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements AfterViewInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);
  private readonly cdr = inject(ChangeDetectorRef);

  @ViewChild('googleBtnRef') googleBtnRef!: ElementRef;

  readonly carregando = this.auth.carregando;
  readonly autenticando = signal(false);
  readonly hasError = signal(false);
  readonly erroMensagem = signal('');

  ngAfterViewInit() {
    if (this.isBrowser) {
      this.iniciarGoogleLogin();
    }
  }

  iniciarGoogleLogin() {
    if (typeof window !== 'undefined' && window.google) {
      if (!environment.googleClientId || environment.googleClientId === 'SEU_GOOGLE_CLIENT_ID') {
        console.error('Google Client ID não configurado. Defina NG_APP_GOOGLE_CLIENT_ID ou GOOGLE_CLIENT_ID no .env.');
        this.hasError.set(true);
        this.erroMensagem.set('Google Client ID não configurado.');
        return;
      }

      const estadoGsi = window.__npGsiState ?? { initialized: false, clientId: undefined };
      const precisaInicializar = !estadoGsi.initialized || estadoGsi.clientId !== environment.googleClientId;

      if (precisaInicializar) {
        window.google.accounts.id.initialize({
          client_id: environment.googleClientId,
          callback: this.handleCredentialResponse.bind(this)
        });
        window.__npGsiState = {
          initialized: true,
          clientId: environment.googleClientId
        };
      }

      this.googleBtnRef.nativeElement.innerHTML = '';
      window.google.accounts.id.renderButton(
        this.googleBtnRef.nativeElement,
        { theme: 'outline', size: 'large', width: '300' }
      );
    } else {
      setTimeout(() => this.iniciarGoogleLogin(), 100);
    }
  }

  async handleCredentialResponse(response: any) {
    this.hasError.set(false);
    this.erroMensagem.set('');
    this.autenticando.set(true);
    this.cdr.markForCheck();
    try {
      const sucesso = await this.auth.loginComGoogle(response.credential);
      if (sucesso) {
        this.router.navigate(['/']);
      } else {
        this.hasError.set(true);
        this.erroMensagem.set('Falha ao autenticar. O servidor pode estar indisponível.');
      }
    } catch {
      this.hasError.set(true);
      this.erroMensagem.set('Erro inesperado. Tente novamente.');
    } finally {
      this.autenticando.set(false);
      this.cdr.markForCheck();
    }
  }
}
