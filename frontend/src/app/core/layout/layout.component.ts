import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { ITENS_MENU } from './menu';

/** Estrutura das telas internas: barra de navegação (menu por perfil) + conteúdo da rota. */
@Component({
  selector: 'app-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './layout.component.html',
})
export class LayoutComponent {
  protected readonly auth = inject(AuthService);

  /** computed: recalcula sozinho quando o usuário logado (signal) muda. */
  protected readonly menu = computed(() => ITENS_MENU.filter((item) => this.auth.temPerfil(item.perfis)));

  sair(): void {
    this.auth.logout();
  }
}
