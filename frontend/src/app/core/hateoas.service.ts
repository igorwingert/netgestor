import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { Colecao, itensDaColecao, Link } from './models/hal.model';

/**
 * Cliente HATEOAS: em vez de montar URLs de acoes no codigo, o frontend usa
 * o "href" dos links que a propria API devolveu.
 *
 * ex.: um contrato ATIVO chega com _links.suspender. a tela mostra o botao "Suspender"
 * so porque o link existe, e ao clicar faz PATCH exatamente nesse href.
 */
@Injectable({ providedIn: 'root' })
export class HateoasService {
  private readonly http = inject(HttpClient);

  /** executa uma acao de mudanca de status (PATCH no link) e devolve o recurso atualizado. */
  executar<T>(link: Link, corpo: unknown = null): Observable<T> {
    return this.http.patch<T>(link.href, corpo);
  }

  /** segue um link para uma colecao relacionada (ex.: _links.faturas do contrato). */
  seguirColecao<T>(link: Link, nome: string): Observable<T[]> {
    return this.http.get<Colecao<T>>(link.href).pipe(map((colecao) => itensDaColecao(colecao, nome)));
  }
}
