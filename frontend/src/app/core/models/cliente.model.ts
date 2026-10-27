import { Recurso } from './hal.model';

export type TipoPessoa = 'FISICA' | 'JURIDICA';

export interface Cliente extends Recurso {
  id: number;
  nome: string;
  cpfCnpj: string;
  tipoPessoa: TipoPessoa;
  email: string;
  telefone: string;
  dataCadastro: string;
  ativo: boolean;
}

export interface ClienteRequest {
  nome: string;
  cpfCnpj: string;
  tipoPessoa: TipoPessoa;
  email: string;
  telefone: string;
  ativo: boolean;
}

export interface Endereco extends Recurso {
  id: number;
  clienteId: number;
  cep: string;
  logradouro: string;
  numero: string;
  complemento: string | null;
  bairro: string;
  cidade: string;
  uf: string;
}

export interface EnderecoRequest {
  cep: string;
  logradouro: string;
  numero: string;
  complemento: string;
  bairro: string;
  cidade: string;
  uf: string;
}
