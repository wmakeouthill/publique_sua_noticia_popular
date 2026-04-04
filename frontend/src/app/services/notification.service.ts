import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'info' | 'warning';

export interface Toast {
  id: number;
  mensagem: string;
  tipo: ToastType;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private nextId = 0;
  readonly toasts = signal<Toast[]>([]);

  success(mensagem: string): void {
    this.adicionar(mensagem, 'success');
  }

  error(mensagem: string): void {
    this.adicionar(mensagem, 'error');
  }

  info(mensagem: string): void {
    this.adicionar(mensagem, 'info');
  }

  warning(mensagem: string): void {
    this.adicionar(mensagem, 'warning');
  }

  remover(id: number): void {
    this.toasts.update(lista => lista.filter(t => t.id !== id));
  }

  private adicionar(mensagem: string, tipo: ToastType): void {
    const id = ++this.nextId;
    this.toasts.update(lista => [...lista, { id, mensagem, tipo }]);
    setTimeout(() => this.remover(id), 4000);
  }
}
