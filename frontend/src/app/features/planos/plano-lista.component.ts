import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { SOMENTE_ADMIN } from '../../core/auth/perfis';
import { Plano } from '../../core/models/plano.model';
import { ToastService } from '../../core/toast.service';
import { PlanoService } from './plano.service';

@Component({
  selector: 'app-plano-lista',
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './plano-lista.component.html',
})
export class PlanoListaComponent implements OnInit {
  private readonly planoService = inject(PlanoService);
  private readonly toast = inject(ToastService);
  private readonly auth = inject(AuthService);

  protected readonly planos = signal<Plano[]>([]);
  /** ATENDENTE só consulta planos; os botões de cadastro/edição aparecem apenas para o ADMIN. */
  protected readonly podeEditar = computed(() => this.auth.temPerfil(SOMENTE_ADMIN));

  ngOnInit(): void {
    this.planoService.listar().subscribe({
      next: (planos) => this.planos.set(planos),
      error: (erro: HttpErrorResponse) => this.toast.erroDaApi(erro),
    });
  }

  excluir(plano: Plano): void {
    if (!confirm(`Excluir o plano ${plano.nome}?`)) return;
    this.planoService.excluir(plano.id).subscribe({
      next: () => {
        this.toast.sucesso(`Plano ${plano.nome} excluído`);
        this.planos.update((lista) => lista.filter((p) => p.id !== plano.id));
      },
      error: (erro: HttpErrorResponse) => this.toast.erroDaApi(erro),
    });
  }
}
