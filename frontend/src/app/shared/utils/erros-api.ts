import { HttpErrorResponse } from '@angular/common/http';
import { AbstractControl, FormGroup } from '@angular/forms';
import { ErroApi } from '../../core/models/erro-api.model';

/** Texto para o usuário: a "mensagem" da API ou um aviso de API fora do ar. */
export function mensagemDeErro(erro: HttpErrorResponse): string {
  if (erro.status === 0) {
    return 'Não foi possível conectar à API. Verifique se o backend está rodando.';
  }
  const corpo = erro.error as ErroApi | null;
  return corpo?.mensagem ?? `Erro inesperado (HTTP ${erro.status})`;
}

/**
 * Erro 400 da API: { "campos": [ { "campo": "cpfCnpj", "mensagem": "CPF inválido..." } ] }.
 * Coloca cada mensagem no controle de mesmo nome do formulário, para aparecer embaixo do campo.
 */
export function aplicarErrosNoFormulario(form: FormGroup, erro: HttpErrorResponse): void {
  const campos = (erro.error as ErroApi | null)?.campos ?? [];
  for (const { campo, mensagem } of campos) {
    const controle = form.get(campo);
    if (controle) {
      controle.setErrors({ api: mensagem });
      controle.markAsTouched();
    }
  }
}

/** Mensagem a exibir embaixo de um campo (validação local ou da API), ou null se estiver tudo certo. */
export function mensagemDoCampo(controle: AbstractControl | null): string | null {
  if (!controle || !controle.touched || !controle.errors) {
    return null;
  }
  const erros = controle.errors;
  if (erros['api']) return erros['api'];
  if (erros['required']) return 'Campo obrigatório';
  if (erros['email']) return 'E-mail inválido';
  if (erros['maxlength']) return `Máximo de ${erros['maxlength'].requiredLength} caracteres`;
  if (erros['min']) return `Valor mínimo: ${erros['min'].min}`;
  if (erros['max']) return `Valor máximo: ${erros['max'].max}`;
  return 'Valor inválido';
}
