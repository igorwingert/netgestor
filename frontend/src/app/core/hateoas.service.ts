import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { Colecao, itensDaColecao, Link } from './models/hal.model';

/**
 * Cliente HATEOAS: em vez de montar URLs de ações no código, o frontend usa
 * o "href" dos links que a própria API devolveu.
 *
 * Ex.: um contrato ATIVO chega com _links.suspender. A tela mostra o botão "Suspender"
 * só porque o link existe, e ao clicar faz PATCH exatamente nesse href.
 */
@Injectable({ providedIn: 'root' })
export class HateoasService {
  private readonly http = inject(HttpClient);

  /** Executa uma ação de mudança de status (PATCH no link) e devolve o recurso atualizado. */
  executar<T>(link: Link, corpo: unknown = null): Observable<T> {
    return this.http.patch<T>(link.href, corpo);
  }

  /** Segue um link para uma coleção relacionada (ex.: _links.faturas do contrato). */
  seguirColecao<T>(link: Link, nome: string): Observable<T[]> {
    return this.http.get<Colecao<T>>(link.href).pipe(map((colecao) => itensDaColecao(colecao, nome)));
  }
}
