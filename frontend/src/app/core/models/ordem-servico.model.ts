import { Recurso } from './hal.model';

export type StatusOrdemServico = 'AGENDADA' | 'EM_EXECUCAO' | 'CONCLUIDA' | 'CANCELADA';
export type TipoOrdemServico = 'INSTALACAO' | 'REPARO' | 'RETIRADA' | 'MUDANCA_ENDERECO';

/** Equipamento listado dentro da OS (relacionamento N:N). */
export interface EquipamentoDaOs {
  id: number;
  tipo: 'ONU' | 'ROTEADOR';
  fabricante: string;
  modelo: string;
  numeroSerie: string;
  mac: string;
  status: string;
}

export interface OrdemServico extends Recurso {
  id: number;
  numero: string;
  contratoId: number;
  contratoNumero: string;
  enderecoInstalacao: string;
  chamadoId: number | null;
  chamadoProtocolo: string | null;
  tecnicoId: number;
  tecnicoNome: string;
  tipo: TipoOrdemServico;
  status: StatusOrdemServico;
  dataAgendada: string;
  dataConclusao: string | null;
  observacoes: string | null;
  equipamentos: EquipamentoDaOs[];
}

export interface Tecnico extends Recurso {
  id: number;
  nome: string;
  telefone: string;
  especialidade: string;
  usuarioId: number;
  usuarioEmail: string;
}
