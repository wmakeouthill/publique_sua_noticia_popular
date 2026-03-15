import { Component, inject, OnInit, AfterViewInit, ElementRef, ViewChild, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { environment } from '../../../environments/environment';

declare global {
  interface Window {
    google: any;
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

  @ViewChild('googleBtnRef') googleBtnRef!: ElementRef;

  readonly carregando = this.auth.carregando;
  hasError = false;

  ngAfterViewInit() {
    if (this.isBrowser) {
      this.iniciarGoogleLogin();
    }
  }

  iniciarGoogleLogin() {
    if (typeof window !== 'undefined' && window.google) {
      if (!environment.googleClientId || environment.googleClientId === 'SEU_GOOGLE_CLIENT_ID') {
        console.error('Google Client ID não configurado. Defina NG_APP_GOOGLE_CLIENT_ID ou GOOGLE_CLIENT_ID no .env.');
        this.hasError = true;
        return;
      }

      window.google.accounts.id.initialize({
        client_id: environment.googleClientId,
        callback: this.handleCredentialResponse.bind(this)
      });

      window.google.accounts.id.renderButton(
        this.googleBtnRef.nativeElement,
        { theme: 'outline', size: 'large', width: '300' }
      );
    } else {
      setTimeout(() => this.iniciarGoogleLogin(), 100);
    }
  }

  async handleCredentialResponse(response: any) {
    this.hasError = false;
    const sucesso = await this.auth.loginComGoogle(response.credential);
    if (sucesso) {
      this.router.navigate(['/']);
    } else {
      this.hasError = true;
    }
  }
}
