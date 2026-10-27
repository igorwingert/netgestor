import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Perfil } from '../models/usuario.model';
import { ToastService } from '../toast.service';
import { AuthService } from './auth.service';

/** Só deixa entrar nas telas internas quem está logado; senão, vai para /login. */
export const autenticadoGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.autenticado() ? true : router.createUrlTree(['/login']);
};

/** A tela de login não faz sentido para quem já está logado. */
export const visitanteGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.autenticado() ? router.createUrlTree(['/']) : true;
};

/**
 * Guard por perfil: a rota informa quem pode acessá-la em data.perfis.
 * Ex.: { path: 'clientes', canActivate: [perfilGuard], data: { perfis: ['ADMIN', 'ATENDENTE'] } }
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
