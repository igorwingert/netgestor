import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ToastsComponent } from './shared/components/toasts.component';

/** Componente raiz: a tela da rota atual + os toasts, que ficam por cima de tudo. */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ToastsComponent],
  template: `
    <router-outlet />
    <app-toasts />
  `,
})
export class App {}
