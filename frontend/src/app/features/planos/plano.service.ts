import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { API_URL } from '../../core/api.config';
import { Colecao, itensDaColecao } from '../../core/models/hal.model';
import { Plano, PlanoRequest } from '../../core/models/plano.model';

/** comunicacao com o recurso /api/planos. */
@Injectable({ providedIn: 'root' })
export class PlanoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/planos`;

  listar(): Observable<Plano[]> {
    return this.http.get<Colecao<Plano>>(this.url).pipe(map((colecao) => itensDaColecao(colecao, 'planos')));
  }

  buscar(id: number): Observable<Plano> {
    return this.http.get<Plano>(`${this.url}/${id}`);
  }

  cadastrar(plano: PlanoRequest): Observable<Plano> {
    return this.http.post<Plano>(this.url, plano);
  }

  atualizar(id: number, plano: PlanoRequest): Observable<Plano> {
    return this.http.put<Plano>(`${this.url}/${id}`, plano);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
