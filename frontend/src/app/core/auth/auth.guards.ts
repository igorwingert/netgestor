import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Perfil } from '../models/usuario.model';
import { ToastService } from '../toast.service';
import { AuthService } from './auth.service';

/** so deixa entrar nas telas internas quem esta logado; senao, vai para /login. */
export const autenticadoGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.autenticado() ? true : router.createUrlTree(['/login']);
};

/** a tela de login nao faz sentido para quem ja esta logado. */
export const visitanteGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.autenticado() ? router.createUrlTree(['/']) : true;
};

/**
 * guard por perfil: a rota informa quem pode acessa-la em data.perfis.
 * ex.: { path: 'clientes', canActivate: [perfilGuard], data: { perfis: ['ADMIN', 'ATENDENTE'] } }
 */
export const perfilGuard: CanActivateFn = (rota) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const toast = inject(ToastService);

  const perfis = rota.data['perfis'] as Perfil[];
  if (auth.temPerfil(perfis)) {
    return true;
  }
  toast.erro('Seu perfil não tem acesso a esta página');
  return router.createUrlTree(['/']);
};
