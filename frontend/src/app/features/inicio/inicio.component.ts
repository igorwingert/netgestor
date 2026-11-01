import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { ITENS_MENU } from '../../core/layout/menu';

/** tela inicial: atalhos para os modulos que o perfil do usuario pode acessar. */
@Component({
  selector: 'app-inicio',
  imports: [RouterLink],
  template: `
    <h1 class="h3">Olá, {{ auth.usuario()?.nome }}!</h1>
    <p class="text-body-secondary">Você entrou com o perfil <strong>{{ auth.usuario()?.perfil }}</strong>.</p>

    <div class="row g-3 mt-2">
      @for (item of modulos(); track item.rota) {
        <div class="col-sm-6 col-lg-4">
          <a class="card h-100 text-decoration-none shadow-sm" [routerLink]="item.rota">
            <div class="card-body">
              <h2 class="h5 card-title"><i [class]="'bi text-primary ' + item.icone"></i> {{ item.rotulo }}</h2>
              <p class="card-text text-body-secondary">{{ item.descricao }}</p>
            </div>
          </a>
        </div>
      }
    </div>
  `,
})
export class InicioComponent {
  protected readonly auth = inject(AuthService);
  protected readonly modulos = computed(() => ITENS_MENU.filter((item) => this.auth.temPerfil(item.perfis)));
}
