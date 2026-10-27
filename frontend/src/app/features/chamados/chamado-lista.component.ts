import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { HateoasService } from '../../core/hateoas.service';
import { Chamado, Prioridade, StatusChamado } from '../../core/models/chamado.model';
import { ToastService } from '../../core/toast.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';
import { ChamadoService } from './chamado.service';

/** Ciclo de vida do chamado: cada botão corresponde a um link que a API pode mandar. */
const ACOES_CHAMADO = [
  { rel: 'iniciar-atendimento', rotulo: 'Iniciar atendimento', icone: 'bi-play-fill', classe: 'btn-outline-primary' },
  { rel: 'resolver', rotulo: 'Resolver', icone: 'bi-check2', classe: 'btn-outline-success' },
  { rel: 'fechar', rotulo: 'Fechar', icone: 'bi-lock', classe: 'btn-outline-secondary' },
];

@Component({
  selector: 'app-chamado-lista',
  imports: [ReactiveFormsModule, RouterLink, StatusBadgeComponent, DatePipe],
  templateUrl: './chamado-lista.component.html',
})
export class ChamadoListaComponent implements OnInit {
  private readonly chamadoService = inject(ChamadoService);
  private readonly hateoas = inject(HateoasService);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly chamados = signal<Chamado[]>([]);
  protected readonly carregando = signal(false);
  protected readonly filtros = this.fb.group({
    status: this.fb.control<StatusChamado | ''>(''),
    prioridade: this.fb.control<Prioridade | ''>(''),
  });
  private consulta?: Subscription;

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.consulta?.unsubscribe(); // cancela a busca anterior (ver ClienteListaComponent)
    this.carregando.set(true);
    this.consulta = this.chamadoService.listar(this.filtros.getRawValue()).subscribe({
      next: (chamados) => {
        this.chamados.set(chamados);
        this.carregando.set(false);
      },
      error: (erro: HttpErrorResponse) => {
        this.toast.erroDaApi(erro);
        this.carregando.set(false);
      },
    });
  }

  /** Só as ações cujo link veio no chamado (ABERTO -> iniciar-atendimento, etc.). */
  acoes(chamado: Chamado) {
    return ACOES_CHAMADO.filter((acao) => chamado._links[acao.rel]);
  }

  executar(chamado: Chamado, acao: (typeof ACOES_CHAMADO)[number]): void {
    this.hateoas.executar<Chamado>(chamado._links[acao.rel]).subscribe({
      next: (atualizado) => {
        // Troca o chamado na lista pela versão nova (novo status e novos links)
        this.chamados.update((lista) => lista.map((c) => (c.id === atualizado.id ? atualizado : c)));
        this.toast.sucesso(`Chamado ${atualizado.protocolo}: ${atualizado.status.replaceAll('_', ' ')}`);
      },
      error: (erro: HttpErrorResponse) => this.toast.erroDaApi(erro),
    });
  }
}
