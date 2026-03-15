export interface PerfilUsuario {
  id: string;
  email: string;
  nome: string;
  avatarUrl: string;
  papel: 'USUARIO' | 'ADMIN';
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  admin: boolean;
  expiresIn: number;
}
