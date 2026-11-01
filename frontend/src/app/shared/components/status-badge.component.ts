import { Component, computed, input } from '@angular/core';

/** cor do Bootstrap para cada status/prioridade que a API devolve. */
const CORES: Record<string, string> = {
  ATIVO: 'success', SUSPENSO: 'warning', CANCELADO: 'secondary',
  ABERTO: 'primary', EM_ATENDIMENTO: 'info', RESOLVIDO: 'success', FECHADO: 'secondary',
  AGENDADA: 'primary', EM_EXECUCAO: 'info', CONCLUIDA: 'success', CANCELADA: 'secondary',
  ABERTA: 'primary', PAGA: 'success', VENCIDA: 'danger',
  BAIXA: 'secondary', MEDIA: 'info', ALTA: 'warning', CRITICA: 'danger',
  INSTALADO: 'success', ESTOQUE: 'secondary', DEFEITO: 'danger',
};

/** uso: <app-status-badge [status]="contrato.status" /> */
@Component({
  selector: 'app-status-badge',
  template: `<span class="badge" [class]="'badge text-bg-' + cor()">{{ texto() }}</span>`,
})
export class StatusBadgeComponent {
  readonly status = input.required<string>();
  protected readonly cor = computed(() => CORES[this.status()] ?? 'secondary');
  protected readonly texto = computed(() => this.status().replaceAll('_', ' '));
}
