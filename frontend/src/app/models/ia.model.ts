export interface PromptRefinarTexto {
  textoAtual: string;
  instrucao: string;
}

export interface ReescreverNoticiaRequest {
  titulo: string;
  conteudo: string;
}

export interface MelhorarTituloRequest {
  tituloAtual: string;
  conteudoResumo?: string;
}

export interface MelhorarSubmancheteRequest {
  submancheteAtual?: string;
  conteudo: string;
}

export interface RespostaIA {
  titulo: string;
  resumo: string;
  conteudo: string;
}
