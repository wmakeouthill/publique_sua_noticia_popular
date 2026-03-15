import { inject } from '@angular/core';
import { CategoriaService } from '../../../services/categoria.service';
import { signal, computed } from '@angular/core';
import { Categoria } from '../../../models/categoria.model';
import { firstValueFrom } from 'rxjs';

export function useCategorias() {
  const categoriaService = inject(CategoriaService);
  
  const categorias = signal<Categoria[]>([]);
  const carregando = signal(false);
  const erro = signal<string | null>(null);

  async function carregar(): Promise<void> {
    carregando.set(true);
    erro.set(null);
    try {
      const dbCategorias = await firstValueFrom(categoriaService.buscarCategorias());
      categorias.set(dbCategorias);
    } catch(e) {
      erro.set('Falha ao buscar categorias.');
    } finally {
      carregando.set(false);
    }
  }

  return {
    categorias: categorias.asReadonly(),
    carregando: carregando.asReadonly(),
    erro: erro.asReadonly(),
    carregar
  };
}
