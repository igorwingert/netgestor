import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { API_URL } from '../../core/api.config';
import { Cliente, ClienteRequest } from '../../core/models/cliente.model';
import { Colecao, itensDaColecao } from '../../core/models/hal.model';
import { criarParametros } from '../../shared/utils/filtros';

/** Comunicação com o recurso /api/clientes. Os componentes nunca usam o HttpClient direto. */
@Injectable({ providedIn: 'root' })
export class ClienteService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/clientes`;

  /** GET /api/clientes?nome=&cpfCnpj= : a resposta HAL traz os itens em _embedded.clientes. */
  listar(filtros: { nome?: string; cpfCnpj?: string } = {}): Observable<Cliente[]> {
    return this.http
      .get<Colecao<Cliente>>(this.url, { params: criarParametros(filtros) })
      .pipe(map((colecao) => itensDaColecao(colecao, 'clientes')));
  }

  buscar(id: number): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.url}/${id}`);
  }

  cadastrar(cliente: ClienteRequest): Observable<Cliente> {
    return this.http.post<Cliente>(this.url, cliente);
  }

  atualizar(id: number, cliente: ClienteRequest): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.url}/${id}`, cliente);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
