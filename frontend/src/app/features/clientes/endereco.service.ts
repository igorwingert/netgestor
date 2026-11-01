import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { API_URL } from '../../core/api.config';
import { Endereco, EnderecoRequest } from '../../core/models/cliente.model';
import { Colecao, itensDaColecao } from '../../core/models/hal.model';

/** endereco e sub-recurso do cliente: /api/clientes/{id}/enderecos e /api/enderecos/{id}. */
@Injectable({ providedIn: 'root' })
export class EnderecoService {
  private readonly http = inject(HttpClient);

  listarDoCliente(clienteId: number): Observable<Endereco[]> {
    return this.http
      .get<Colecao<Endereco>>(`${API_URL}/clientes/${clienteId}/enderecos`)
      .pipe(map((colecao) => itensDaColecao(colecao, 'enderecos')));
  }

  cadastrar(clienteId: number, endereco: EnderecoRequest): Observable<Endereco> {
    return this.http.post<Endereco>(`${API_URL}/clientes/${clienteId}/enderecos`, endereco);
  }

  atualizar(id: number, endereco: EnderecoRequest): Observable<Endereco> {
    return this.http.put<Endereco>(`${API_URL}/enderecos/${id}`, endereco);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/enderecos/${id}`);
  }
}
