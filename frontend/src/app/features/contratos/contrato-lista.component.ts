import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { ATENDIMENTO } from '../../core/auth/perfis';
import { Contrato, StatusContrato } from '../../core/models/contrato.model';
import { ToastService } from '../../core/toast.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';
import { ContratoService } from './contrato.service';

@Component({
  selector: 'app-contrato-lista',
  imports: [ReactiveFormsModule, RouterLink, StatusBadgeComponent],
  templateUrl: './contrato-lista.component.html',
})
export class ContratoListaComponent implements OnInit {
  private readonly contratoService = inject(ContratoService);
  private readonly toast = inject(ToastService);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly contratos = signal<Contrato[]>([]);
  protected readonly carregando = signal(false);
  /** O TECNICO consulta contratos, mas não cadastra nem altera. */
  protected readonly podeGerenciar = computed(() => this.auth.temPerfil(ATENDIMENTO));
  protected readonly filtros = this.fb.group({ status: this.fb.control<StatusContrato | ''>('') });
  private consulta?: Subscription;

  ngOnInit(): void {
    this.carregar();
  }

  /** GET /api/contratos?status=... */
  carregar(): void {
    this.consulta?.unsubscribe(); // cancela a busca anterior (ver ClienteListaComponent)
    this.carregando.set(true);
    this.consulta = this.contratoService.listar(this.filtros.getRawValue()).subscribe({
      next: (contratos) => {
        this.contratos.set(contratos);
        this.carregando.set(false);
      },
      error: (erro: HttpErrorResponse) => {
        this.toast.erroDaApi(erro);
        this.carregando.set(false);
      },
    });
  }
}
