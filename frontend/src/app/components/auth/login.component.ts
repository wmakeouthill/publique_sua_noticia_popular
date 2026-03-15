import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

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
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly carregando = this.auth.carregando;
  hasError = false;

  async handleCredentialResponse(response: any) {
    this.hasError = false;
    const sucesso = await this.auth.loginComGoogle(response.credential);
    if (sucesso) {
      this.router.navigate(['/']);
    } else {
      this.hasError = true;
    }
  }

  // Na vida real os scripts do Google dariam trigger no window.google.accounts.id.initialize
  // Aqui você chama um fake login só para mock test se o script de fora nao rodar
  mockLogin() {
    this.handleCredentialResponse({ credential: 'mock.jwt.token' });
  }
}
