/**
 * tipos do formato HAL (HATEOAS) usado pela API.
 *
 * Recurso:  { "id": 1, ..., "_links": { "self": { "href": "..." }, "suspender": { "href": "..." } } }
 * colecao:  { "_embedded": { "clientes": [ ... ] }, "_links": { "self": { "href": "..." } } }
 */
export interface Link {
  href: string;
  templated?: boolean;
}

/** todo recurso traz _links: o proprio recurso, os relacionados e as acoes permitidas agora. */
export interface Recurso {
  _links: Record<string, Link>;
}

export interface Colecao<T> {
  _embedded?: Record<string, T[]>;
  _links: Record<string, Link>;
}

/** extrai os itens de uma colecao HAL. lista vazia vem sem _embedded, por isso o "?? []". */
export function itensDaColecao<T>(colecao: Colecao<T>, nome: string): T[] {
  return colecao._embedded?.[nome] ?? [];
}
