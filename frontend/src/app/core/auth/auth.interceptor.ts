import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { API_URL } from '../api.config';
import { ToastService } from '../toast.service';
import { AuthService } from './auth.service';

/**
 * Interceptor: passa por TODAS as requisições feitas com o HttpClient.
 *
 * 1. Na ida: se houver token, acrescenta o header "Authorization: Bearer <token>"
 *    (só para a nossa API, nunca para outros sites).
 * 2. Na volta: se a API responder 401 (token ausente, inválido ou expirado),
 *    faz logout e leva o usuário para a tela de login.
 */
export const authInterceptor: HttpInterceptorFn = (requisicao, proximo) => {
  const auth = inject(AuthService);
  const toast = inject(ToastService);

  const token = auth.token;
  const ehNossaApi = requisicao.url.startsWith(API_URL);
  const requisicaoComToken =
    token && ehNossaApi ? requisicao.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : requisicao;

  return proximo(requisicaoComToken).pipe(
    catchError((erro: HttpErrorResponse) => {
      const ehLogin = requisicao.url.endsWith('/auth/login');
      if (erro.status === 401 && !ehLogin) {
        toast.erro(erro.error?.mensagem ?? 'Sessão expirada. Faça login novamente.');
        auth.logout();
      }
      return throwError(() => erro);
    }),
  );
};
