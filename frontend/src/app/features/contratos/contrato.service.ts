import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { API_URL } from '../../core/api.config';
import { Contrato, ContratoRequest, StatusContrato } from '../../core/models/contrato.model';
import { Colecao, itensDaColecao } from '../../core/models/hal.model';
import { criarParametros } from '../../shared/utils/filtros';

/**
 * Comunicação com o recurso /api/contratos.
 * As ações (suspender, reativar, cancelar) NÃO estão aqui: elas são executadas pelo
 * HateoasService, usando o link que veio no próprio contrato.
 */
@Injectable({ providedIn: 'root' })
export class ContratoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/contratos`;

  /** GET /api/contratos?status=ATIVO&clienteId=1 */
  listar(filtros: { status?: StatusContrato | ''; clienteId?: number | null } = {}): Observable<Contrato[]> {
    return this.http
      .get<Colecao<Contrato>>(this.url, { params: criarParametros(filtros) })
      .pipe(map((colecao) => itensDaColecao(colecao, 'contratos')));
  }

  buscar(id: number): Observable<Contrato> {
    return this.http.get<Contrato>(`${this.url}/${id}`);
  }

  cadastrar(contrato: ContratoRequest): Observable<Contrato> {
    return this.http.post<Contrato>(this.url, contrato);
  }

  atualizar(id: number, contrato: ContratoRequest): Observable<Contrato> {
    return this.http.put<Contrato>(`${this.url}/${id}`, contrato);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
