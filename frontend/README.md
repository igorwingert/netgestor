# NetGestor - Frontend (Angular)

Interface web do NetGestor, que consome a API REST do backend (`../backend`).

```bash
npm install
npm start          # http://localhost:4200 (a API precisa estar rodando em http://localhost:8080)
npm run build      # gera a versão de produção em dist/
```

Estrutura de `src/app`:

- `core/`: autenticação (service, interceptor, guards), modelos, layout e serviços transversais (toasts, HATEOAS);
- `features/<recurso>/`: telas e o service de cada recurso (clientes, planos, contratos, chamados, ordens de serviço);
- `shared/`: componentes, pipe e funções reutilizáveis.

Instruções completas no README da raiz do projeto.
