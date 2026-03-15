export interface PerfilUsuario {
  id: string;
  email: string;
  nome: string;
  avatarUrl: string;
  papel: 'USUARIO' | 'ADMIN';
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  expiraEm: string;
}
