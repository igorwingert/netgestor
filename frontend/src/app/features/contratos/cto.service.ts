import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { API_URL } from '../../core/api.config';
import { Cto, OcupacaoCto } from '../../core/models/contrato.model';
import { Colecao, itensDaColecao } from '../../core/models/hal.model';

/** CTOs e ocupação das portas: usados no formulário de contrato para escolher uma porta livre. */
@Injectable({ providedIn: 'root' })
export class CtoService {
  private readonly http = inject(HttpClient);

  listar(): Observable<Cto[]> {
    return this.http.get<Colecao<Cto>>(`${API_URL}/ctos`).pipe(map((colecao) => itensDaColecao(colecao, 'ctos')));
  }

  /** GET /api/ctos/{id}/ocupacao */
  ocupacao(ctoId: number): Observable<OcupacaoCto> {
    return this.http.get<OcupacaoCto>(`${API_URL}/ctos/${ctoId}/ocupacao`);
  }
}
