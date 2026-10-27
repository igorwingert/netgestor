import { Recurso } from './hal.model';

export type Tecnologia = 'FIBRA' | 'RADIO';

export interface Plano extends Recurso {
  id: number;
  nome: string;
  downloadMbps: number;
  uploadMbps: number;
  valorMensal: number;
  tecnologia: Tecnologia;
  ativo: boolean;
}

export interface PlanoRequest {
  nome: string;
  downloadMbps: number | null;
  uploadMbps: number | null;
  valorMensal: number | null;
  tecnologia: Tecnologia;
  ativo: boolean;
}
