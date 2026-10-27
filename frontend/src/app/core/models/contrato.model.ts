import { Recurso } from './hal.model';

export type StatusContrato = 'ATIVO' | 'SUSPENSO' | 'CANCELADO';

export interface Contrato extends Recurso {
  id: number;
  numero: string;
  clienteId: number;
  clienteNome: string;
  planoId: number;
  planoNome: string;
  enderecoInstalacaoId: number;
  enderecoInstalacao: string;
  ctoId: number | null;
  ctoIdentificacao: string | null;
  portaCto: number | null;
  dataInicio: string;
  diaVencimento: number;
  status: StatusContrato;
}

export interface ContratoRequest {
  clienteId: number | null;
  enderecoInstalacaoId: number | null;
  planoId: number | null;
  ctoId: number | null;
  portaCto: number | null;
  dataInicio: string;
  diaVencimento: number;
}

/** CTO: caixa de terminação óptica, onde o contrato de fibra ocupa uma porta. */
export interface Cto extends Recurso {
  id: number;
  identificacao: string;
  oltId: number;
  oltNome: string;
  portaPon: number;
  capacidade: number;
}

/** Resposta de GET /api/ctos/{id}/ocupacao. */
export interface OcupacaoCto {
  ctoId: number;
  identificacao: string;
  capacidade: number;
  portasOcupadas: number;
  portasLivres: number;
  portas: { numero: number; livre: boolean; contratoNumero: string | null; clienteNome: string | null }[];
}

export type StatusFatura = 'ABERTA' | 'PAGA' | 'VENCIDA' | 'CANCELADA';

export interface Fatura extends Recurso {
  id: number;
  competencia: string;
  valor: number;
  valorPago: number;
  dataVencimento: string;
  status: StatusFatura;
}
