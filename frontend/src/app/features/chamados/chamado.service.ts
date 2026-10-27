import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { API_URL } from '../../core/api.config';
import { Chamado, ChamadoRequest, Prioridade, StatusChamado } from '../../core/models/chamado.model';
import { Colecao, itensDaColecao } from '../../core/models/hal.model';
import { criarParametros } from '../../shared/utils/filtros';

/** Comunicação com /api/chamados. As mudanças de status usam os links (HateoasService). */
@Injectable({ providedIn: 'root' })
export class ChamadoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/chamados`;

  /** GET /api/chamados?status=&prioridade= */
  listar(filtros: { status?: StatusChamado | ''; prioridade?: Prioridade | '' } = {}): Observable<Chamado[]> {
    return this.http
      .get<Colecao<Chamado>>(this.url, { params: criarParametros(filtros) })
      .pipe(map((colecao) => itensDaColecao(colecao, 'chamados')));
  }

  /** Quem abre o chamado é o usuário do token: a API pega essa informação do JWT. */
  abrir(chamado: ChamadoRequest): Observable<Chamado> {
    return this.http.post<Chamado>(this.url, chamado);
  }
}
