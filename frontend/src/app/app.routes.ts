import { Routes } from '@angular/router';

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
    path: '**',
    redirectTo: ''
  }
];
