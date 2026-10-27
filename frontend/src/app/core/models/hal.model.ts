/**
 * Tipos do formato HAL (HATEOAS) usado pela API.
 *
 * Recurso:  { "id": 1, ..., "_links": { "self": { "href": "..." }, "suspender": { "href": "..." } } }
 * Coleção:  { "_embedded": { "clientes": [ ... ] }, "_links": { "self": { "href": "..." } } }
 */
export interface Link {
  href: string;
  templated?: boolean;
}

/** Todo recurso traz _links: o próprio recurso, os relacionados e as ações permitidas agora. */
export interface Recurso {
  _links: Record<string, Link>;
}

export interface Colecao<T> {
  _embedded?: Record<string, T[]>;
  _links: Record<string, Link>;
}

/** Extrai os itens de uma coleção HAL. Lista vazia vem sem _embedded, por isso o "?? []". */
export function itensDaColecao<T>(colecao: Colecao<T>, nome: string): T[] {
  return colecao._embedded?.[nome] ?? [];
}
