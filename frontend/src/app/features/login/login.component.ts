import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { ToastService } from '../../core/toast.service';
import { mensagemDeErro, mensagemDoCampo } from '../../shared/utils/erros-api';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(NonNullableFormBuilder);

  /** estado da tela em signals: com OnPush (padrao no Angular 22), a tela e redesenhada quando eles mudam. */
  protected readonly entrando = signal(false);
  protected readonly erroLogin = signal<string | null>(null);

  protected readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', Validators.required],
  });

  /** usuarios da migration V2, para facilitar a demonstracao. */
  protected readonly usuariosDeTeste = [
    { perfil: 'ADMIN', email: 'admin@netgestor.com', senha: 'admin123' },
    { perfil: 'ATENDENTE', email: 'atendente@netgestor.com', senha: 'atendente123' },
    { perfil: 'TECNICO', email: 'tecnico@netgestor.com', senha: 'tecnico123' },
  ];

  entrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.entrando.set(true);
    this.erroLogin.set(null);

    const { email, senha } = this.form.getRawValue();
    this.auth.login(email, senha).subscribe({
      next: (resposta) => {
        this.toast.sucesso(`Bem-vindo(a), ${resposta.usuario.nome}!`);
        this.router.navigate(['/']);
      },
      error: (erro: HttpErrorResponse) => {
        // 401 -> "E-mail ou senha inválidos" (mensagem vinda da API)
        this.erroLogin.set(mensagemDeErro(erro));
        this.entrando.set(false);
      },
    });
  }

  preencher(email: string, senha: string): void {
    this.form.setValue({ email, senha });
  }

  erro(campo: string): string | null {
    return mensagemDoCampo(this.form.get(campo));
  }
}
