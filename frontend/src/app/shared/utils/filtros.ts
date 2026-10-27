import { HttpParams } from '@angular/common/http';

/**
 * Monta os parâmetros de consulta (?status=ATIVO&clienteId=1) ignorando os filtros vazios.
 * Filtro vazio = a API não filtra por aquele campo.
 */
export function criarParametros(filtros: Record<string, string | number | null | undefined>): HttpParams {
  let parametros = new HttpParams();
  for (const [nome, valor] of Object.entries(filtros)) {
    if (valor !== null && valor !== undefined && valor !== '') {
      parametros = parametros.set(nome, String(valor));
    }
  }
  return parametros;
}
