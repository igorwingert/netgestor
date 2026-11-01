import { Component, inject } from '@angular/core';
import { ToastService } from '../../core/toast.service';

/** exibe as mensagens do ToastService no canto da tela (verde = sucesso, vermelho = erro). */
@Component({
  selector: 'app-toasts',
  template: `
    <!-- canto inferior direito: não cobre os botões do cabeçalho (Voltar, Sair) -->
    <div class="toast-container position-fixed bottom-0 end-0 p-3">
      @for (toast of toastService.toasts(); track toast.id) {
        <div
          class="toast show align-items-center border-0"
          [class.text-bg-success]="toast.tipo === 'sucesso'"
          [class.text-bg-danger]="toast.tipo === 'erro'"
          role="alert"
        >
          <div class="d-flex">
            <div class="toast-body">
              <i class="bi me-1" [class.bi-check-circle]="toast.tipo === 'sucesso'"
                 [class.bi-exclamation-triangle]="toast.tipo === 'erro'"></i>
              {{ toast.mensagem }}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" aria-label="Fechar"
                    (click)="toastService.fechar(toast.id)"></button>
          </div>
        </div>
      }
    </div>
  `,
})
export class ToastsComponent {
  protected readonly toastService = inject(ToastService);
}
