import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminService, AdminDashboard } from '../../../services/admin.service';
import { NotificationService } from '../../../services/notification.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly notification = inject(NotificationService);

  readonly dashboard = signal<AdminDashboard | null>(null);
  readonly carregando = signal(false);

  ngOnInit(): void {
    this.carregar();
  }

  async carregar(): Promise<void> {
    this.carregando.set(true);
    try {
      const data = await firstValueFrom(this.adminService.dashboard());
      this.dashboard.set(data);
    } catch {
      this.notification.error('Falha ao carregar o dashboard.');
    } finally {
      this.carregando.set(false);
    }
  }
}
