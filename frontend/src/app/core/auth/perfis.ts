import { Perfil } from '../models/usuario.model';

/**
 * Grupos de perfis usados nas rotas (guards) e no menu.
 * Espelham as regras do @PreAuthorize da API: a API é quem protege de verdade;
 * no frontend, isso serve para não mostrar telas que dariam 403.
 */
export const TODOS: Perfil[] = ['ADMIN', 'ATENDENTE', 'TECNICO'];
export const ATENDIMENTO: Perfil[] = ['ADMIN', 'ATENDENTE'];
export const SOMENTE_ADMIN: Perfil[] = ['ADMIN'];
