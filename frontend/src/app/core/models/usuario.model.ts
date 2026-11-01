export type Perfil = 'ADMIN' | 'ATENDENTE' | 'TECNICO';

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  perfil: Perfil;
  ativo: boolean;
}

/** resposta do POST /api/auth/login. */
export interface LoginResponse {
  token: string;
  tipo: string;
  expiraEmSegundos: number;
  usuario: Usuario;
}
