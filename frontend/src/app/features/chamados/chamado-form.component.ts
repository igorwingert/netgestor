import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Prioridade, TipoChamado } from '../../core/models/chamado.model';
import { Contrato } from '../../core/models/contrato.model';
import { ToastService } from '../../core/toast.service';
import { aplicarErrosNoFormulario, mensagemDoCampo } from '../../shared/utils/erros-api';
import { ContratoService } from '../contratos/contrato.service';
import { ChamadoService } from './chamado.service';

/** Abertura de chamado (rota /chamados/novo). */
@Component({
  selector: 'app-chamado-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './chamado-form.component.html',
})
export class ChamadoFormComponent implements OnInit {
  private readonly chamadoService = inject(ChamadoService);
  private readonly contratoService = inject(ContratoService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly contratos = signal<Contrato[]>([]);
  protected readonly salvando = signal(false);

  protected readonly form = this.fb.group({
    contratoId: this.fb.control<number | null>(null, Validators.required),
    tipo: this.fb.control<TipoChamado>('SEM_CONEXAO'),
    prioridade: this.fb.control<Prioridade>('MEDIA'),
    descricao: ['', [Validators.required, Validators.maxLength(1000)]],
  });

  ngOnInit(): void {
    // Contrato cancelado não aceita chamados (regra da API): nem oferecemos na lista
    this.contratoService.listar().subscribe((contratos) =>
      this.contratos.set(contratos.filter((c) => c.status !== 'CANCELADO')),
    );
  }

  abrir(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.salvando.set(true);
    this.chamadoService.abrir(this.form.getRawValue()).subscribe({
      next: (chamado) => {
        this.toast.sucesso(`Chamado aberto: protocolo ${chamado.protocolo}`);
        this.router.navigate(['/chamados']);
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
