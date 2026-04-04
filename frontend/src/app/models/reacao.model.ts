export type AlvoTipo = 'NOTICIA' | 'COMENTARIO';

export interface ReacaoStatus {
  total: number;
  likedByMe: boolean;
}

export interface ToggleReacaoRequest {
  alvoTipo: AlvoTipo;
  alvoId: string;
}
