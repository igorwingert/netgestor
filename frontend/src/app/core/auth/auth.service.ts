import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { API_URL } from '../api.config';
import { LoginResponse, Perfil, Usuario } from '../models/usuario.model';

const CHAVE_TOKEN = 'netgestor.token';
const CHAVE_USUARIO = 'netgestor.usuario';

/**
 * Autenticação no frontend.
 *
 * - login(): chama POST /api/auth/login e guarda o token JWT e os dados do usuário no localStorage
 *   (assim o login continua valendo se a página for recarregada);
 * - o token é enviado em todas as requisições pelo authInterceptor;
 * - usuario() é um signal: menu, guards e telas reagem automaticamente ao login/logout.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly usuarioLogado = signal<Usuario | null>(this.lerUsuarioSalvo());

  /** Usuário logado (somente leitura para o resto da aplicação). */
  readonly usuario = this.usuarioLogado.asReadonly();
  readonly autenticado = computed(() => this.usuarioLogado() !== null);

  login(email: string, senha: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API_URL}/auth/login`, { email, senha }).pipe(
      tap((resposta) => {
        localStorage.setItem(CHAVE_TOKEN, resposta.token);
        localStorage.setItem(CHAVE_USUARIO, JSON.stringify(resposta.usuario));
        this.usuarioLogado.set(resposta.usuario);
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(CHAVE_TOKEN);
    localStorage.removeItem(CHAVE_USUARIO);
    this.usuarioLogado.set(null);
    this.router.navigate(['/login']);
  }

  get token(): string | null {
    return localStorage.getItem(CHAVE_TOKEN);
  }

  temPerfil(perfis: Perfil[]): boolean {
    const usuario = this.usuarioLogado();
    return usuario !== null && perfis.includes(usuario.perfil);
  }

  /** Ao abrir a aplicação, recupera o login salvo, desde que o token ainda não tenha expirado. */
  private lerUsuarioSalvo(): Usuario | null {
    const token = localStorage.getItem(CHAVE_TOKEN);
    const usuario = localStorage.getItem(CHAVE_USUARIO);
    if (!token || !usuario || this.tokenExpirado(token)) {
      localStorage.removeItem(CHAVE_TOKEN);
      localStorage.removeItem(CHAVE_USUARIO);
      return null;
    }
    return JSON.parse(usuario) as Usuario;
  }

  /**
   * O payload do JWT é apenas Base64 (não é criptografado), então dá para ler
   * a data de expiração ("exp", em segundos) sem chamar a API.
   */
  private tokenExpirado(token: string): boolean {
    try {
      const payloadBase64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const payload = JSON.parse(atob(payloadBase64)) as { exp: number };
      return payload.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }
}
