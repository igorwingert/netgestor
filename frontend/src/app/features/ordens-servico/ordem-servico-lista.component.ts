import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { HateoasService } from '../../core/hateoas.service';
import { OrdemServico, StatusOrdemServico, Tecnico } from '../../core/models/ordem-servico.model';
import { ToastService } from '../../core/toast.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';
import { OrdemServicoService } from './ordem-servico.service';

const ACOES_OS = [
  { rel: 'iniciar', rotulo: 'Iniciar', icone: 'bi-play-fill', classe: 'btn-primary' },
  { rel: 'concluir', rotulo: 'Concluir', icone: 'bi-check2-circle', classe: 'btn-success' },
  { rel: 'cancelar', rotulo: 'Cancelar', icone: 'bi-x-circle', classe: 'btn-outline-danger' },
];

/**
 * Ordens de serviço.
 * - TECNICO: vê a própria agenda ("Minhas ordens de serviço") e inicia/conclui pelos links.
 * - ADMIN/ATENDENTE: veem todas, com filtro por técnico.
 * Os links já chegam filtrados pela API conforme o perfil (o técnico recebe iniciar/concluir,
 * a atendente recebe cancelar), então a tela apenas exibe o que veio.
 */
@Component({
  selector: 'app-ordem-servico-lista',
  imports: [ReactiveFormsModule, StatusBadgeComponent, DatePipe],
  templateUrl: './ordem-servico-lista.component.html',
})
export class OrdemServicoListaComponent implements OnInit {
  private readonly ordemServicoService = inject(OrdemServicoService);
  private readonly hateoas = inject(HateoasService);
  private readonly toast = inject(ToastService);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly ordens = signal<OrdemServico[]>([]);
  protected readonly tecnicos = signal<Tecnico[]>([]);
  protected readonly carregando = signal(false);
  protected readonly ehTecnico = computed(() => this.auth.usuario()?.perfil === 'TECNICO');

  protected readonly filtros = this.fb.group({
    status: this.fb.control<StatusOrdemServico | ''>(''),
    tecnicoId: this.fb.control<number | null>(null),
  });
  private consulta?: Subscription;

  ngOnInit(): void {
    this.ordemServicoService.listarTecnicos().subscribe({
      next: (tecnicos) => {
        this.tecnicos.set(tecnicos);
        if (this.ehTecnico()) {
          // O técnico logado é o Tecnico cujo usuário é o dono do token
          const meuCadastro = tecnicos.find((t) => t.usuarioId === this.auth.usuario()?.id);
          this.filtros.controls.tecnicoId.setValue(meuCadastro?.id ?? null);
        }
        this.carregar();
      },
      error: (erro: HttpErrorResponse) => this.toast.erroDaApi(erro),
    });
  }

  carregar(): void {
    this.consulta?.unsubscribe(); // cancela a busca anterior (ver ClienteListaComponent)
    this.carregando.set(true);
    this.consulta = this.ordemServicoService.listar(this.filtros.getRawValue()).subscribe({
      next: (ordens) => {
        this.ordens.set(ordens);
        this.carregando.set(false);
      },
      error: (erro: HttpErrorResponse) => {
        this.toast.erroDaApi(erro);
        this.carregando.set(false);
      },
    });
  }

  acoes(os: OrdemServico) {
    return ACOES_OS.filter((acao) => os._links[acao.rel]);
  }

  executar(os: OrdemServico, acao: (typeof ACOES_OS)[number]): void {
    if (acao.rel === 'cancelar' && !confirm(`Cancelar a ordem de serviço ${os.numero}?`)) return;
    this.hateoas.executar<OrdemServico>(os._links[acao.rel]).subscribe({
      next: (atualizada) => {
        this.ordens.update((lista) => lista.map((o) => (o.id === atualizada.id ? atualizada : o)));
        this.toast.sucesso(`OS ${atualizada.numero}: ${atualizada.status.replaceAll('_', ' ')}`);
      },
      error: (erro: HttpErrorResponse) => this.toast.erroDaApi(erro),
    });
  }
}
