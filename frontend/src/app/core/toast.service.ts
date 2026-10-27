import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { mensagemDeErro } from '../shared/utils/erros-api';

export interface Toast {
  id: number;
  tipo: 'sucesso' | 'erro';
  mensagem: string;
}

/** Mensagens rápidas (toasts) de sucesso e erro, exibidas pelo ToastsComponent. */
@Injectable({ providedIn: 'root' })
export class ToastService {
  private proximoId = 1;
  readonly toasts = signal<Toast[]>([]);

  sucesso(mensagem: string): void {
    this.mostrar('sucesso', mensagem);
  }

  erro(mensagem: string): void {
    this.mostrar('erro', mensagem);
  }

  /** Mostra a "mensagem" que veio da API. O 401 é ignorado: o interceptor já trata. */
  erroDaApi(erro: HttpErrorResponse): void {
    if (erro.status !== 401) {
      this.mostrar('erro', mensagemDeErro(erro));
    }
  }

  fechar(id: number): void {
    this.toasts.update((lista) => lista.filter((t) => t.id !== id));
  }

  private mostrar(tipo: Toast['tipo'], mensagem: string): void {
    const id = this.proximoId++;
    this.toasts.update((lista) => [...lista, { id, tipo, mensagem }]);
    setTimeout(() => this.fechar(id), 5000);
  }
}
