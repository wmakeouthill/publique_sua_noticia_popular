import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminService } from '../../../services/admin.service';
import { NotificationService } from '../../../services/notification.service';
import { PerfilUsuario } from '../../../models/usuario.model';
import { TempoRelativoPipe } from '../../../shared/pipes/tempo-relativo.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-admin-usuarios',
  standalone: true,
  imports: [CommonModule, RouterModule, TempoRelativoPipe],
  templateUrl: './admin-usuarios.component.html',
  styleUrl: './admin-usuarios.component.css'
})
export class AdminUsuariosComponent implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly notification = inject(NotificationService);

  readonly usuarios = signal<PerfilUsuario[]>([]);
  readonly carregando = signal(false);
  readonly alterando = signal<string | null>(null);

  ngOnInit(): void {
    this.carregar();
  }

  async carregar(): Promise<void> {
    this.carregando.set(true);
    try {
      const lista = await firstValueFrom(this.adminService.listarUsuarios());
      this.usuarios.set(lista);
    } catch {
      this.notification.error('Falha ao carregar usuários.');
    } finally {
      this.carregando.set(false);
    }
  }

  async alterarStatus(usuario: PerfilUsuario): Promise<void> {
    this.alterando.set(usuario.id);
    try {
      const atualizado = await firstValueFrom(
        this.adminService.alterarStatusUsuario(usuario.id, !usuario.ativo)
      );
      this.usuarios.update(lista =>
        lista.map(u => u.id === atualizado.id ? atualizado : u)
      );
      this.notification.success(
        atualizado.ativo ? `${atualizado.nome} ativado.` : `${atualizado.nome} desativado.`
      );
    } catch {
      this.notification.error('Erro ao alterar status do usuário.');
    } finally {
      this.alterando.set(null);
    }
  }
}
