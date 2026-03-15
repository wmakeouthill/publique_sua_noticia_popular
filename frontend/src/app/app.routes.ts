import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

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
    title: 'Notícia Popular - Lendo Agora'
  },
  {
    path: 'nova-noticia',
    loadComponent: () => import('./components/editor/editor.component').then(m => m.EditorComponent),
    title: 'Notícia Popular - Criar',
    canActivate: [authGuard]
  },
  {
    path: 'editar-noticia/:id',
    loadComponent: () => import('./components/editor/editor.component').then(m => m.EditorComponent),
    title: 'Notícia Popular - Editar',
    canActivate: [authGuard]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
