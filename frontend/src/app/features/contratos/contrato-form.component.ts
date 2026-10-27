import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Cliente, Endereco } from '../../core/models/cliente.model';
import { Contrato, Cto } from '../../core/models/contrato.model';
import { Plano } from '../../core/models/plano.model';
import { ToastService } from '../../core/toast.service';
import { aplicarErrosNoFormulario, mensagemDoCampo } from '../../shared/utils/erros-api';
import { ClienteService } from '../clientes/cliente.service';
import { EnderecoService } from '../clientes/endereco.service';
import { PlanoService } from '../planos/plano.service';
import { ContratoService } from './contrato.service';
import { CtoService } from './cto.service';

/**
 * Cadastro e edição de contrato (rotas /contratos/novo e /contratos/:id/editar).
 *
 * O formulário reage às escolhas do usuário:
 * - escolheu o cliente -> carrega os endereços DELE;
 * - escolheu um plano via rádio -> esconde CTO e porta;
 * - escolheu a CTO -> consulta /ctos/{id}/ocupacao e oferece só as portas livres.
 */
@Component({
  selector: 'app-contrato-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './contrato-form.component.html',
})
export class ContratoFormComponent implements OnInit {
  private readonly contratoService = inject(ContratoService);
  private readonly clienteService = inject(ClienteService);
  private readonly enderecoService = inject(EnderecoService);
  private readonly planoService = inject(PlanoService);
  private readonly ctoService = inject(CtoService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);
  private readonly fb = inject(NonNullableFormBuilder);

  readonly id = input<string>();
  protected readonly contratoId = computed(() => (this.id() ? Number(this.id()) : null));

  protected readonly clientes = signal<Cliente[]>([]);
  protected readonly enderecos = signal<Endereco[]>([]);
  protected readonly planos = signal<Plano[]>([]);
  protected readonly ctos = signal<Cto[]>([]);
  protected readonly portasLivres = signal<number[]>([]);
  protected readonly ehFibra = signal(true);
  protected readonly salvando = signal(false);
  /** Na edição: o contrato como estava antes (a porta atual dele também deve aparecer na lista). */
  private readonly original = signal<Contrato | null>(null);

  /** Planos à venda + o plano atual do contrato (mesmo que tenha saído de venda). */
  protected readonly planosDisponiveis = computed(() =>
    this.planos().filter((p) => p.ativo || p.id === this.original()?.planoId),
  );

  protected readonly form = this.fb.group({
    clienteId: this.fb.control<number | null>(null, Validators.required),
    enderecoInstalacaoId: this.fb.control<number | null>(null, Validators.required),
    planoId: this.fb.control<number | null>(null, Validators.required),
    ctoId: this.fb.control<number | null>(null),
    portaCto: this.fb.control<number | null>(null),
    dataInicio: [new Date().toISOString().slice(0, 10), Validators.required],
    diaVencimento: [10, [Validators.required, Validators.min(1), Validators.max(28)]],
  });

  constructor() {
    // valueChanges avisa cada mudança do campo; takeUntilDestroyed encerra a inscrição ao sair da tela
    const campos = this.form.controls;
    campos.clienteId.valueChanges.pipe(takeUntilDestroyed()).subscribe((id) => this.carregarEnderecos(id));
    campos.planoId.valueChanges.pipe(takeUntilDestroyed()).subscribe((id) => this.aoEscolherPlano(id));
    campos.ctoId.valueChanges.pipe(takeUntilDestroyed()).subscribe((id) => this.carregarPortasLivres(id));
  }

  ngOnInit(): void {
    this.clienteService.listar().subscribe((clientes) => this.clientes.set(clientes));
    this.ctoService.listar().subscribe((ctos) => this.ctos.set(ctos));
    this.planoService.listar().subscribe((planos) => {
      this.planos.set(planos);
      this.aoEscolherPlano(this.form.controls.planoId.value);
    });

    const id = this.contratoId();
    if (id) {
      this.contratoService.buscar(id).subscribe({
        next: (contrato) => {
          this.original.set(contrato);
          this.form.patchValue(contrato);
        },
        error: (erro: HttpErrorResponse) => {
          this.toast.erroDaApi(erro);
          this.router.navigate(['/contratos']);
        },
      });
    }
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.salvando.set(true);
    const id = this.contratoId();
    const dados = this.form.getRawValue();
    const requisicao = id ? this.contratoService.atualizar(id, dados) : this.contratoService.cadastrar(dados);

    requisicao.subscribe({
      next: (contrato) => {
        this.toast.sucesso(`Contrato ${contrato.numero} salvo`);
        this.router.navigate(['/contratos', contrato.id]);
      },
      error: (erro: HttpErrorResponse) => {
        // 409 com a regra violada, ex.: "A porta 1 da CTO CTO-CEN-01 já está ocupada pelo contrato ..."
        this.salvando.set(false);
        aplicarErrosNoFormulario(this.form, erro);
        this.toast.erroDaApi(erro);
      },
    });
  }

  erro(campo: string): string | null {
    return mensagemDoCampo(this.form.get(campo));
  }

  private carregarEnderecos(clienteId: number | null): void {
    this.enderecos.set([]);
    if (!clienteId) return;
    this.enderecoService.listarDoCliente(clienteId).subscribe((enderecos) => {
      this.enderecos.set(enderecos);
      // Se o endereço escolhido não é deste cliente, limpa a escolha
      const atual = this.form.controls.enderecoInstalacaoId.value;
      if (!enderecos.some((e) => e.id === atual)) {
        this.form.controls.enderecoInstalacaoId.setValue(null);
      }
    });
  }

  private aoEscolherPlano(planoId: number | null): void {
    const plano = this.planos().find((p) => p.id === planoId);
    const fibra = plano?.tecnologia !== 'RADIO';
    this.ehFibra.set(fibra);
    if (!fibra) {
      // Plano via rádio não usa CTO (regra da API)
      this.form.patchValue({ ctoId: null, portaCto: null });
    }
  }

  private carregarPortasLivres(ctoId: number | null): void {
    this.portasLivres.set([]);
    if (!ctoId) return;
    this.ctoService.ocupacao(ctoId).subscribe((ocupacao) => {
      const livres = ocupacao.portas.filter((p) => p.livre).map((p) => p.numero);
      const original = this.original();
      if (original?.ctoId === ctoId && original.portaCto) {
        livres.push(original.portaCto); // a porta que o próprio contrato já usa
        livres.sort((a, b) => a - b);
      }
      this.portasLivres.set(livres);
      if (!livres.includes(this.form.controls.portaCto.value ?? -1)) {
        this.form.controls.portaCto.setValue(null);
      }
    });
  }
}
