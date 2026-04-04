export type StatusNoticia = 'RASCUNHO' | 'PUBLICADA' | 'ARQUIVADA';

export interface NoticiaResumo {
  id: string;
  titulo: string;
  resumo: string;
  imagemUrl: string;
  categoriaId: string;
  autorId: string;
  autorNome: string | null;
  autorAvatarUrl: string | null;
  status: StatusNoticia;
  visualizacoes: number;
  totalLikes: number;
  likedByMe: boolean;
  criadoEm: string;
  publicadoEm: string;
}

export interface Noticia extends NoticiaResumo {
  conteudo: string; // JSON text para o editor
  atualizadoEm: string;
}

export interface CriarNoticiaRequest {
  titulo: string;
  conteudo: string;
  resumo: string;
  imagemUrl?: string;
  categoriaId: string;
  publicarImediatamente: boolean;
}

export interface EditarNoticiaRequest {
  titulo?: string;
  conteudo?: string;
  resumo?: string;
  imagemUrl?: string | null;
}
