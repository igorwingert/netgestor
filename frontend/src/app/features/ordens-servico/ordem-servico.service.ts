import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { API_URL } from '../../core/api.config';
import { Colecao, itensDaColecao } from '../../core/models/hal.model';
import { OrdemServico, StatusOrdemServico, Tecnico } from '../../core/models/ordem-servico.model';
import { criarParametros } from '../../shared/utils/filtros';

/** comunicacao com /api/ordens-servico e /api/tecnicos. as mudancas de status usam os links. */
@Injectable({ providedIn: 'root' })
export class OrdemServicoService {
  private readonly http = inject(HttpClient);

  /** GET /api/ordens-servico?status=&tecnicoId= */
  listar(filtros: { status?: StatusOrdemServico | ''; tecnicoId?: number | null } = {}): Observable<OrdemServico[]> {
    return this.http
      .get<Colecao<OrdemServico>>(`${API_URL}/ordens-servico`, { params: criarParametros(filtros) })
      .pipe(map((colecao) => itensDaColecao(colecao, 'ordensServico')));
  }

  listarTecnicos(): Observable<Tecnico[]> {
    return this.http
      .get<Colecao<Tecnico>>(`${API_URL}/tecnicos`)
      .pipe(map((colecao) => itensDaColecao(colecao, 'tecnicos')));
  }
}
