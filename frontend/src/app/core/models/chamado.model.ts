import { Recurso } from './hal.model';

export type StatusChamado = 'ABERTO' | 'EM_ATENDIMENTO' | 'RESOLVIDO' | 'FECHADO';
export type Prioridade = 'BAIXA' | 'MEDIA' | 'ALTA' | 'CRITICA';
export type TipoChamado = 'SEM_CONEXAO' | 'LENTIDAO' | 'FINANCEIRO' | 'OUTRO';

export interface Chamado extends Recurso {
  id: number;
  protocolo: string;
  contratoId: number;
  contratoNumero: string;
  clienteNome: string;
  abertoPorId: number;
  abertoPorNome: string;
  tipo: TipoChamado;
  prioridade: Prioridade;
  status: StatusChamado;
  descricao: string;
  dataAbertura: string;
  dataFechamento: string | null;
}

export interface ChamadoRequest {
  contratoId: number | null;
  tipo: TipoChamado;
  prioridade: Prioridade;
  descricao: string;
}
