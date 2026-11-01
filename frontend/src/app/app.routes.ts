import { Routes } from '@angular/router';
import { autenticadoGuard, perfilGuard, visitanteGuard } from './core/auth/auth.guards';
import { ATENDIMENTO, SOMENTE_ADMIN, TODOS } from './core/auth/perfis';
import { LayoutComponent } from './core/layout/layout.component';
import { InicioComponent } from './features/inicio/inicio.component';
import { LoginComponent } from './features/login/login.component';

/**
 * rotas da aplicacao.
 * - autenticadoGuard: todas as telas internas exigem login;
 * - perfilGuard + data.perfis: cada modulo so abre para os perfis com acesso na API;
 * - loadComponent: as telas sao carregadas sob demanda (lazy loading), deixando o carregamento inicial menor.
 */
export const routes: Routes = [
  { path: 'login', component: LoginComponent, canActivate: [visitanteGuard], title: 'Login | NetGestor' },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [autenticadoGuard],
    children: [
      { path: '', component: InicioComponent, title: 'Início | NetGestor' },
      {
        path: 'clientes',
        canActivate: [perfilGuard],
        data: { perfis: ATENDIMENTO },
        title: 'Clientes | NetGestor',
        children: [
          { path: '', loadComponent: () => import('./features/clientes/cliente-lista.component').then((m) => m.ClienteListaComponent) },
          { path: 'novo', loadComponent: () => import('./features/clientes/cliente-form.component').then((m) => m.ClienteFormComponent) },
          { path: ':id', loadComponent: () => import('./features/clientes/cliente-form.component').then((m) => m.ClienteFormComponent) },
        ],
      },
      {
        path: 'planos',
        canActivate: [perfilGuard],
        data: { perfis: ATENDIMENTO },
        title: 'Planos | NetGestor',
        children: [
          { path: '', loadComponent: () => import('./features/planos/plano-lista.component').then((m) => m.PlanoListaComponent) },
          {
            path: 'novo', canActivate: [perfilGuard], data: { perfis: SOMENTE_ADMIN },
            loadComponent: () => import('./features/planos/plano-form.component').then((m) => m.PlanoFormComponent),
          },
          {
            path: ':id', canActivate: [perfilGuard], data: { perfis: SOMENTE_ADMIN },
            loadComponent: () => import('./features/planos/plano-form.component').then((m) => m.PlanoFormComponent),
          },
        ],
      },
      {
        path: 'contratos',
        canActivate: [perfilGuard],
        data: { perfis: TODOS },
        title: 'Contratos | NetGestor',
        children: [
          { path: '', loadComponent: () => import('./features/contratos/contrato-lista.component').then((m) => m.ContratoListaComponent) },
          {
            path: 'novo', canActivate: [perfilGuard], data: { perfis: ATENDIMENTO },
            loadComponent: () => import('./features/contratos/contrato-form.component').then((m) => m.ContratoFormComponent),
          },
          {
            path: ':id/editar', canActivate: [perfilGuard], data: { perfis: ATENDIMENTO },
            loadComponent: () => import('./features/contratos/contrato-form.component').then((m) => m.ContratoFormComponent),
          },
          { path: ':id', loadComponent: () => import('./features/contratos/contrato-detalhe.component').then((m) => m.ContratoDetalheComponent) },
        ],
      },
      {
        path: 'chamados',
        canActivate: [perfilGuard],
        data: { perfis: ATENDIMENTO },
        title: 'Chamados | NetGestor',
        children: [
          { path: '', loadComponent: () => import('./features/chamados/chamado-lista.component').then((m) => m.ChamadoListaComponent) },
          { path: 'novo', loadComponent: () => import('./features/chamados/chamado-form.component').then((m) => m.ChamadoFormComponent) },
        ],
      },
      {
        path: 'ordens-servico',
        canActivate: [perfilGuard],
        data: { perfis: TODOS },
        title: 'Ordens de serviço | NetGestor',
        loadComponent: () =>
          import('./features/ordens-servico/ordem-servico-lista.component').then((m) => m.OrdemServicoListaComponent),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
