export interface Comentario {
  id: string;
  noticiaId: string;
  autorId: string;
  autorNome: string;
  autorAvatarUrl: string | null;
  conteudo: string;
  criadoEm: string;
}

export interface CriarComentarioRequest {
  conteudo: string;
}
