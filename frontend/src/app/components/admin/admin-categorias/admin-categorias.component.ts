import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CategoriaService } from '../../../services/categoria.service';
import { NotificationService } from '../../../services/notification.service';
import { Categoria } from '../../../models/categoria.model';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-admin-categorias',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './admin-categorias.component.html',
  styleUrl: './admin-categorias.component.css'
})
export class AdminCategoriasComponent implements OnInit {
  private readonly categoriaService = inject(CategoriaService);
  private readonly notification = inject(NotificationService);
  private readonly fb = inject(FormBuilder);

  readonly categorias = signal<Categoria[]>([]);
  readonly carregando = signal(false);
  readonly salvando = signal(false);
  readonly mostrarForm = signal(false);
  readonly editandoId = signal<string | null>(null);

  form = this.fb.group({
    nome:     ['', [Validators.required, Validators.minLength(2)]],
    descricao:[''],
    icone:    ['']
  });

  ngOnInit(): void {
    this.carregar();
  }

  async carregar(): Promise<void> {
    this.carregando.set(true);
    try {
      const lista = await firstValueFrom(this.categoriaService.buscarCategorias());
      this.categorias.set(lista);
    } catch {
      this.notification.error('Falha ao carregar categorias.');
    } finally {
      this.carregando.set(false);
    }
  }

  abrirFormNova(): void {
    this.editandoId.set(null);
    this.form.reset({ nome: '', descricao: '', icone: '' });
    this.mostrarForm.set(true);
  }

  abrirFormEditar(cat: Categoria): void {
    this.editandoId.set(cat.id);
    this.form.patchValue({ nome: cat.nome, descricao: cat.descricao, icone: cat.icone });
    this.mostrarForm.set(true);
  }

  cancelar(): void {
    this.mostrarForm.set(false);
    this.editandoId.set(null);
  }

  async salvar(): Promise<void> {
    if (this.form.invalid) return;
    this.salvando.set(true);
    const { nome, descricao, icone } = this.form.value;

    try {
      if (this.editandoId()) {
        const atualizada = await firstValueFrom(
          this.categoriaService.atualizar(this.editandoId()!, { nome: nome!, descricao: descricao!, icone: icone! })
        );
        this.categorias.update(lista =>
          lista.map(c => c.id === atualizada.id ? atualizada : c)
        );
        this.notification.success('Categoria atualizada.');
      } else {
        const nova = await firstValueFrom(
          this.categoriaService.criar({ nome: nome!, descricao: descricao!, icone: icone! })
        );
        this.categorias.update(lista => [...lista, nova]);
        this.notification.success('Categoria criada.');
      }
      this.cancelar();
    } catch {
      this.notification.error('Erro ao salvar categoria.');
    } finally {
      this.salvando.set(false);
    }
  }

  async desativar(cat: Categoria): Promise<void> {
    if (!confirm(`Desativar a categoria "${cat.nome}"?`)) return;
    try {
      await firstValueFrom(this.categoriaService.desativar(cat.id));
      this.categorias.update(lista => lista.filter(c => c.id !== cat.id));
      this.notification.success('Categoria desativada.');
    } catch {
      this.notification.error('Erro ao desativar categoria.');
    }
  }
}
