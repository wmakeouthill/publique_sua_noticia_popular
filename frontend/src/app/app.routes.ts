import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/feed/feed.component').then(m => m.FeedComponent),
    title: 'Notícia Popular - Home'
  },
  {
    path: 'login',
    loadComponent: () => import('./components/auth/login.component').then(m => m.LoginComponent),
    title: 'Entrar - Notícia Popular'
  },
  {
    path: 'noticia/:id',
    loadComponent: () => import('./components/noticia-detalhe/noticia-detalhe.component').then(m => m.NoticiaDetalheComponent),
    title: 'Notícia Popular'
  },
  {
    path: 'nova-noticia',
    loadComponent: () => import('./components/editor/editor.component').then(m => m.EditorComponent),
    title: 'Nova Notícia - Notícia Popular',
    canActivate: [authGuard]
  },
  {
    path: 'editar-noticia/:id',
    loadComponent: () => import('./components/editor/editor.component').then(m => m.EditorComponent),
    title: 'Editar Notícia - Notícia Popular',
    canActivate: [authGuard]
  },
  {
    path: 'minhas-noticias',
    loadComponent: () => import('./components/perfil/minhas-noticias/minhas-noticias.component').then(m => m.MinhasNoticiasComponent),
    title: 'Minhas Publicações - Notícia Popular',
    canActivate: [authGuard]
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./components/admin/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent),
        title: 'Admin Dashboard'
      },
      {
        path: 'usuarios',
        loadComponent: () => import('./components/admin/admin-usuarios/admin-usuarios.component').then(m => m.AdminUsuariosComponent),
        title: 'Gerenciar Usuários'
      },
      {
        path: 'categorias',
        loadComponent: () => import('./components/admin/admin-categorias/admin-categorias.component').then(m => m.AdminCategoriasComponent),
        title: 'Gerenciar Categorias'
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
