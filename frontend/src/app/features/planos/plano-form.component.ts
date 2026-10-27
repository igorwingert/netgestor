import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Tecnologia } from '../../core/models/plano.model';
import { ToastService } from '../../core/toast.service';
import { aplicarErrosNoFormulario, mensagemDoCampo } from '../../shared/utils/erros-api';
import { PlanoService } from './plano.service';

/** Cadastro e edição de plano (rotas /planos/novo e /planos/:id, só ADMIN). */
@Component({
  selector: 'app-plano-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './plano-form.component.html',
})
export class PlanoFormComponent implements OnInit {
  private readonly planoService = inject(PlanoService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);
  private readonly fb = inject(NonNullableFormBuilder);

  readonly id = input<string>();
  protected readonly planoId = computed(() => (this.id() ? Number(this.id()) : null));
  protected readonly salvando = signal(false);

  protected readonly form = this.fb.group({
    nome: ['', [Validators.required, Validators.maxLength(80)]],
    downloadMbps: this.fb.control<number | null>(null, [Validators.required, Validators.min(1)]),
    uploadMbps: this.fb.control<number | null>(null, [Validators.required, Validators.min(1)]),
    valorMensal: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.01)]),
    tecnologia: this.fb.control<Tecnologia>('FIBRA'),
    ativo: [true],
  });

  ngOnInit(): void {
    const id = this.planoId();
    if (id) {
      this.planoService.buscar(id).subscribe({
        next: (plano) => this.form.patchValue(plano),
        error: (erro: HttpErrorResponse) => {
          this.toast.erroDaApi(erro);
          this.router.navigate(['/planos']);
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
    const id = this.planoId();
    const dados = this.form.getRawValue();
    const requisicao = id ? this.planoService.atualizar(id, dados) : this.planoService.cadastrar(dados);

    requisicao.subscribe({
      next: (plano) => {
        this.toast.sucesso(`Plano ${plano.nome} salvo`);
        this.router.navigate(['/planos']);
      },
      error: (erro: HttpErrorResponse) => {
        this.salvando.set(false);
        aplicarErrosNoFormulario(this.form, erro);
        this.toast.erroDaApi(erro);
      },
    });
  }

  erro(campo: string): string | null {
    return mensagemDoCampo(this.form.get(campo));
  }
}
