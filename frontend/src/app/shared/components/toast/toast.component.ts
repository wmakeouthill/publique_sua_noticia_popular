import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      @for (toast of notification.toasts(); track toast.id) {
        <div class="toast toast-{{ toast.tipo }}" (click)="notification.remover(toast.id)">
          <span class="toast-icon">{{ icone(toast.tipo) }}</span>
          <span class="toast-msg">{{ toast.mensagem }}</span>
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      bottom: 1.5rem;
      right: 1.5rem;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }
    .toast {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.875rem 1.25rem;
      border-radius: 0.75rem;
      font-size: 0.9rem;
      font-weight: 500;
      cursor: pointer;
      min-width: 260px;
      max-width: 420px;
      backdrop-filter: blur(12px);
      animation: slideIn 0.2s ease;
      border: 1px solid rgba(255,255,255,0.1);
    }
    .toast-success { background: rgba(34,197,94,0.15); color: #4ade80; }
    .toast-error   { background: rgba(239,68,68,0.15);  color: #f87171; }
    .toast-info    { background: rgba(59,130,246,0.15); color: #60a5fa; }
    .toast-warning { background: rgba(234,179,8,0.15);  color: #facc15; }
    .toast-icon { font-size: 1.1rem; }
    @keyframes slideIn {
      from { opacity: 0; transform: translateX(100%); }
      to   { opacity: 1; transform: translateX(0); }
    }
  `]
})
export class ToastComponent {
  readonly notification = inject(NotificationService);

  icone(tipo: string): string {
    const map: Record<string, string> = {
      success: '✅', error: '❌', info: 'ℹ️', warning: '⚠️'
    };
    return map[tipo] ?? 'ℹ️';
  }
}
