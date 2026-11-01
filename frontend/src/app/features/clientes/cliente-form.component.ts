import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Endereco, TipoPessoa } from '../../core/models/cliente.model';
import { ToastService } from '../../core/toast.service';
import { aplicarErrosNoFormulario, mensagemDoCampo } from '../../shared/utils/erros-api';
import { ClienteService } from './cliente.service';
import { EnderecoService } from './endereco.service';

/**
 * cadastro e edicao de cliente (rotas /clientes/novo e /clientes/:id).
 * na edicao, mostra tambem os enderecos do cliente (sub-recurso).
 */
@Component({
  selector: 'app-cliente-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './cliente-form.component.html',
})
export class ClienteFormComponent implements OnInit {
  private readonly clienteService = inject(ClienteService);
  private readonly enderecoService = inject(EnderecoService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);
  private readonly fb = inject(NonNullableFormBuilder);

  /** parametro :id da rota, recebido como input (withComponentInputBinding no app.config). */
  readonly id = input<string>();
  protected readonly clienteId = computed(() => (this.id() ? Number(this.id()) : null));

  protected readonly salvando = signal(false);
  protected readonly enderecos = signal<Endereco[]>([]);
  /** null = cadastrando um endereco novo; numero = editando aquele endereco. */
  protected readonly enderecoEmEdicao = signal<number | null>(null);

  protected readonly form = this.fb.group({
    nome: ['', [Validators.required, Validators.maxLength(150)]],
    cpfCnpj: ['', Validators.required],
    tipoPessoa: this.fb.control<TipoPessoa>('FISICA', Validators.required),
    email: ['', [Validators.required, Validators.email]],
    telefone: ['', Validators.required],
    ativo: [true],
  });

  protected readonly formEndereco = this.fb.group({
    cep: ['', Validators.required],
    logradouro: ['', Validators.required],
    numero: ['', Validators.required],
    complemento: [''],
    bairro: ['', Validators.required],
    cidade: ['', Validators.required],
    uf: ['PR', Validators.required],
  });

  ngOnInit(): void {
    const id = this.clienteId();
    if (id) {
      this.clienteService.buscar(id).subscribe({
        next: (cliente) => this.form.patchValue(cliente),
        error: (erro: HttpErrorResponse) => {
          this.toast.erroDaApi(erro);
          this.router.navigate(['/clientes']);
        },
      });
      this.carregarEnderecos(id);
    }
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.salvando.set(true);
    const id = this.clienteId();
    const dados = this.form.getRawValue();
    const requisicao = id ? this.clienteService.atualizar(id, dados) : this.clienteService.cadastrar(dados);

    requisicao.subscribe({
      next: (cliente) => {
        this.salvando.set(false);
        if (id) {
          this.toast.sucesso('Cliente atualizado');
          this.router.navigate(['/clientes']);
        } else {
          // depois de cadastrar, abre a edicao para o usuario incluir os enderecos
          this.toast.sucesso('Cliente cadastrado. Agora cadastre os endereços.');
          this.router.navigate(['/clientes', cliente.id]);
        }
      },
      error: (erro: HttpErrorResponse) => {
        this.salvando.set(false);
        aplicarErrosNoFormulario(this.form, erro); // 400: erro embaixo de cada campo
        this.toast.erroDaApi(erro); // 409: ex. "Já existe um cliente com o CPF/CNPJ ..."
      },
    });
  }

  // ---- enderecos (sub-recurso) ----

  private carregarEnderecos(clienteId: number): void {
    this.enderecoService.listarDoCliente(clienteId).subscribe({
      next: (enderecos) => this.enderecos.set(enderecos),
      error: (erro: HttpErrorResponse) => this.toast.erroDaApi(erro),
    });
  }

  editarEndereco(endereco: Endereco): void {
    this.enderecoEmEdicao.set(endereco.id);
    this.formEndereco.patchValue({ ...endereco, complemento: endereco.complemento ?? '' });
  }

  cancelarEndereco(): void {
    this.enderecoEmEdicao.set(null);
    this.formEndereco.reset();
  }

  salvarEndereco(): void {
    const clienteId = this.clienteId();
    if (!clienteId) return;
    if (this.formEndereco.invalid) {
      this.formEndereco.markAllAsTouched();
      return;
    }
    const dados = this.formEndereco.getRawValue();
    const enderecoId = this.enderecoEmEdicao();
    const requisicao = enderecoId
      ? this.enderecoService.atualizar(enderecoId, dados)
      : this.enderecoService.cadastrar(clienteId, dados);

    requisicao.subscribe({
      next: () => {
        this.toast.sucesso(enderecoId ? 'Endereço atualizado' : 'Endereço cadastrado');
        this.cancelarEndereco();
        this.carregarEnderecos(clienteId);
      },
      error: (erro: HttpErrorResponse) => {
        aplicarErrosNoFormulario(this.formEndereco, erro);
        this.toast.erroDaApi(erro);
      },
    });
  }

  excluirEndereco(endereco: Endereco): void {
    if (!confirm(`Excluir o endereço ${endereco.logradouro}, ${endereco.numero}?`)) return;
    this.enderecoService.excluir(endereco.id).subscribe({
      next: () => {
        this.toast.sucesso('Endereço excluído');
        this.enderecos.update((lista) => lista.filter((e) => e.id !== endereco.id));
      },
      error: (erro: HttpErrorResponse) => this.toast.erroDaApi(erro),
    });
  }

  erro(campo: string): string | null {
    return mensagemDoCampo(this.form.get(campo));
  }

  erroEndereco(campo: string): string | null {
    return mensagemDoCampo(this.formEndereco.get(campo));
  }
}
