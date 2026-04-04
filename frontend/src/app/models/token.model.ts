export interface TokenPayload {
  sub: string;
  papel?: string;
  tipo: 'access' | 'refresh';
  iat: number;
  exp: number;
}
