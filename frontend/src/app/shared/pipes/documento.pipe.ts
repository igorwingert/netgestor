import { Pipe, PipeTransform } from '@angular/core';

/**
 * A API guarda o documento sem máscara; na tela mostramos formatado. Uso: {{ cliente.cpfCnpj | documento }}
 * O CNPJ pode ser alfanumérico (ex.: 12.ABC.345/01DE-35), por isso [0-9A-Z] nas 12 primeiras posições.
 */
@Pipe({ name: 'documento' })
export class DocumentoPipe implements PipeTransform {
  transform(valor: string | null | undefined): string {
    if (!valor) return '';
    if (valor.length === 11) {
      return valor.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
    }
    if (valor.length === 14) {
      return valor.replace(/([0-9A-Z]{2})([0-9A-Z]{3})([0-9A-Z]{3})([0-9A-Z]{4})(\d{2})/, '$1.$2.$3/$4-$5');
    }
    return valor;
  }
}
