import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { Cliente } from '../../core/models/cliente.model';
import { ToastService } from '../../core/toast.service';
import { DocumentoPipe } from '../../shared/pipes/documento.pipe';
import { ClienteService } from './cliente.service';

@Component({
  selector: 'app-cliente-lista',
  imports: [ReactiveFormsModule, RouterLink, DocumentoPipe],
  templateUrl: './cliente-lista.component.html',
})
export class ClienteListaComponent implements OnInit {
  private readonly clienteService = inject(ClienteService);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly clientes = signal<Cliente[]>([]);
  protected readonly carregando = signal(false);
  protected readonly filtros = this.fb.group({ nome: [''], cpfCnpj: [''] });
  private consulta?: Subscription;

  ngOnInit(): void {
    this.carregar();
  }

  /** Consulta específica da API: GET /api/clientes?nome=...&cpfCnpj=... */
  carregar(): void {
    // Se uma busca anterior ainda estiver em andamento, cancela: só vale a resposta da última.
    // (Sem isso, uma resposta lenta e antiga poderia chegar depois e sobrescrever a lista filtrada.)
    this.consulta?.unsubscribe();
    this.carregando.set(true);
    this.consulta = this.clienteService.listar(this.filtros.getRawValue()).subscribe({
      next: (clientes) => {
        this.clientes.set(clientes);
        this.carregando.set(false);
      },
      error: (erro: HttpErrorResponse) => {
        this.toast.erroDaApi(erro);
        this.carregando.set(false);
      },
    });
  }

  limparFiltros(): void {
    this.filtros.reset();
    this.carregar();
  }

  excluir(cliente: Cliente): void {
    if (!confirm(`Excluir o cliente ${cliente.nome}?`)) {
      return;
    }
    this.clienteService.excluir(cliente.id).subscribe({
      next: () => {
        this.toast.sucesso(`Cliente ${cliente.nome} excluído`);
        this.clientes.update((lista) => lista.filter((c) => c.id !== cliente.id));
      },
      // 409 quando o cliente tem contratos: a mensagem da API explica o motivo
      error: (erro: HttpErrorResponse) => this.toast.erroDaApi(erro),
    });
  }
}
