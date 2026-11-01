import { Perfil } from '../models/usuario.model';

/**
 * grupos de perfis usados nas rotas (guards) e no menu.
 * espelham as regras do @PreAuthorize da API: a API e quem protege de verdade;
 * no frontend, isso serve para nao mostrar telas que dariam 403.
 */
export const TODOS: Perfil[] = ['ADMIN', 'ATENDENTE', 'TECNICO'];
export const ATENDIMENTO: Perfil[] = ['ADMIN', 'ATENDENTE'];
export const SOMENTE_ADMIN: Perfil[] = ['ADMIN'];
