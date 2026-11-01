import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { ATENDIMENTO } from '../../core/auth/perfis';
import { HateoasService } from '../../core/hateoas.service';
import { Chamado } from '../../core/models/chamado.model';
import { Contrato, Fatura } from '../../core/models/contrato.model';
import { EquipamentoDaOs, OrdemServico } from '../../core/models/ordem-servico.model';
import { ToastService } from '../../core/toast.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';
import { ContratoService } from './contrato.service';

/** as acoes possiveis de um contrato. o botao so aparece se a API mandar o link (rel) correspondente. */
const ACOES_CONTRATO = [
  { rel: 'suspender', rotulo: 'Suspender', icone: 'bi-pause-circle', classe: 'btn-warning' },
  { rel: 'reativar', rotulo: 'Reativar', icone: 'bi-play-circle', classe: 'btn-success' },
  { rel: 'cancelar', rotulo: 'Cancelar contrato', icone: 'bi-x-circle', classe: 'btn-danger' },
];

/**
 * detalhe do contrato: o melhor exemplo de HATEOAS no frontend.
 *
 * - os botoes de acao sao escolhidos pelos _links que a API devolveu:
 *   ATIVO -> suspender/cancelar; SUSPENSO -> reativar/cancelar; CANCELADO -> nenhum.
 *   o frontend nao repete a regra de negocio: ele apenas segue os links.
 * - as listas relacionadas (faturas, chamados, OS, equipamentos) sao buscadas
 *   seguindo os links "faturas", "chamados"... se o perfil nao pode ve-las, o link
 *   nem vem na resposta e a secao nao e exibida.
 */
@Component({
  selector: 'app-contrato-detalhe',
  imports: [RouterLink, StatusBadgeComponent, CurrencyPipe, DatePipe],
  templateUrl: './contrato-detalhe.component.html',
})
export class ContratoDetalheComponent implements OnInit {
  private readonly contratoService = inject(ContratoService);
  private readonly hateoas = inject(HateoasService);
  private readonly toast = inject(ToastService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly id = input.required<string>();

  protected readonly contrato = signal<Contrato | null>(null);
  protected readonly executando = signal(false);
  protected readonly faturas = signal<Fatura[] | null>(null);
  protected readonly chamados = signal<Chamado[] | null>(null);
  protected readonly ordens = signal<OrdemServico[] | null>(null);
  protected readonly equipamentos = signal<EquipamentoDaOs[] | null>(null);

  protected readonly podeGerenciar = computed(() => this.auth.temPerfil(ATENDIMENTO));

  /** acoes disponiveis = acoes cujo link esta presente no contrato. */
  protected readonly acoes = computed(() => {
    const contrato = this.contrato();
    return contrato ? ACOES_CONTRATO.filter((acao) => contrato._links[acao.rel]) : [];
  });

  ngOnInit(): void {
    this.contratoService.buscar(Number(this.id())).subscribe({
      next: (contrato) => {
        this.contrato.set(contrato);
        this.carregarRelacionados(contrato);
      },
      error: (erro: HttpErrorResponse) => {
        this.toast.erroDaApi(erro);
        this.router.navigate(['/contratos']);
      },
    });
  }

  /** PATCH no href do link da acao. a resposta e o contrato com o novo status e os novos links. */
  executar(acao: (typeof ACOES_CONTRATO)[number]): void {
    const contrato = this.contrato();
    if (!contrato || !confirm(`${acao.rotulo} o contrato ${contrato.numero}?`)) return;

    this.executando.set(true);
    this.hateoas.executar<Contrato>(contrato._links[acao.rel]).subscribe({
      next: (atualizado) => {
        this.contrato.set(atualizado);
        this.executando.set(false);
        this.toast.sucesso(`Contrato ${atualizado.numero} agora está ${atualizado.status}`);
      },
      error: (erro: HttpErrorResponse) => {
        this.executando.set(false);
        this.toast.erroDaApi(erro);
      },
    });
  }

  excluir(): void {
    const contrato = this.contrato();
    if (!contrato || !confirm(`Excluir o contrato ${contrato.numero}?`)) return;
    this.contratoService.excluir(contrato.id).subscribe({
      next: () => {
        this.toast.sucesso(`Contrato ${contrato.numero} excluído`);
        this.router.navigate(['/contratos']);
      },
      // 409 se tiver faturas/chamados/OS: a API sugere usar a acao de cancelar
      error: (erro: HttpErrorResponse) => this.toast.erroDaApi(erro),
    });
  }

  private carregarRelacionados(contrato: Contrato): void {
    const links = contrato._links;
    if (links['faturas']) {
      this.hateoas.seguirColecao<Fatura>(links['faturas'], 'faturas').subscribe((lista) => this.faturas.set(lista));
    }
    if (links['chamados']) {
      this.hateoas.seguirColecao<Chamado>(links['chamados'], 'chamados').subscribe((lista) => this.chamados.set(lista));
    }
    if (links['ordensServico']) {
      this.hateoas.seguirColecao<OrdemServico>(links['ordensServico'], 'ordensServico').subscribe((lista) => this.ordens.set(lista));
    }
    if (links['equipamentos']) {
      this.hateoas.seguirColecao<EquipamentoDaOs>(links['equipamentos'], 'equipamentos').subscribe((lista) => this.equipamentos.set(lista));
    }
  }
}
