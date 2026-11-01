import { HttpParams } from '@angular/common/http';

/**
 * monta os parametros de consulta (?status=ATIVO&clienteId=1) ignorando os filtros vazios.
 * filtro vazio = a API nao filtra por aquele campo.
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
