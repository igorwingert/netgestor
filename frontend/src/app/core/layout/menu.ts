import { ATENDIMENTO, TODOS } from '../auth/perfis';
import { Perfil } from '../models/usuario.model';

export interface ItemMenu {
  rota: string;
  rotulo: string;
  icone: string;
  descricao: string;
  perfis: Perfil[];
}

/** Itens do menu. Cada usuário só vê os itens do seu perfil. */
export const ITENS_MENU: ItemMenu[] = [
  { rota: '/clientes', rotulo: 'Clientes', icone: 'bi-people', perfis: ATENDIMENTO,
    descricao: 'Cadastro de clientes e seus endereços' },
  { rota: '/planos', rotulo: 'Planos', icone: 'bi-speedometer2', perfis: ATENDIMENTO,
    descricao: 'Planos de internet oferecidos' },
  { rota: '/contratos', rotulo: 'Contratos', icone: 'bi-file-earmark-text', perfis: TODOS,
    descricao: 'Contratos, portas de CTO e mudanças de status' },
  { rota: '/chamados', rotulo: 'Chamados', icone: 'bi-headset', perfis: ATENDIMENTO,
    descricao: 'Abertura e acompanhamento de chamados' },
  { rota: '/ordens-servico', rotulo: 'Ordens de serviço', icone: 'bi-tools', perfis: TODOS,
    descricao: 'Agenda e execução dos serviços de campo' },
];
