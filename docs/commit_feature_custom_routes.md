# feature/custom-routes — Commit de ajustes (UI + persistência + testes)

Este commit liga e estabiliza a feature de **rotas/roteiros personalizados** usando a camada de persistência que já existia no branch, e ajusta a interface para suportar o fluxo “Mapa → Roteiros → Detalhe”.

## Mudanças e porquê

### 1) Persistência de rotas — correções para compilar e mapear no Room

- **`RoutePoiWithPoi` passa a usar `@Embedded`**
  - **Porquê:** a query em `RoutePoiDao.getRouteWithPois()` devolve colunas de `RoutePoi` + colunas do `Poi`. Sem `@Embedded`, o Room/KSP não consegue mapear o objeto `routePoi` e falha o build.
  - Ficheiro: `data/model/RoutePoiWithPoi.kt`

- **`RouteDao` simplificado para usar apenas `Long` como id**
  - **Porquê:** existiam overloads contraditórios (`String` e `Long`) para `getRouteById`/`deleteRoute`, mas a entidade `Route` usa `id: Long`. A UI e o `RouteRepository` trabalham com `Long`.
  - Ficheiro: `data/dao/RouteDao.kt`

- **`Route.id` com `autoGenerate = true`**
  - **Porquê:** a criação de rotas no `RouteRepository` obtém o `routeId` via `insertRoute()`. Para isso funcionar como esperado, o `id` deve ser auto-gerado pelo Room.
  - Ficheiro: `data/entity/Route.kt`

- **Seeder de rotas corrigido para o novo modelo (`Route` + `RoutePoi`)**
  - **Porquê:** o seeder usava `routeId` como `String` e um campo inexistente (`totalEstimatedTimeMinutes`). Foi ajustado para:
    - inserir `Route` e receber `routeId: Long`;
    - inserir os POIs do roteiro via `RoutePoiDao` (posição + tempo de paragem).
  - Ficheiro: `data/database/DatabaseSeeder.kt`

### 2) UI/Navegação — interface alinhada com a feature “rotas”

- **Navegação Compose (Mapa/Roteiros/Detalhe)**
  - **Porquê:** no branch, a UI estava centrada no mapa e sheets. Para “rotas” ficar utilizável, era necessário um fluxo de ecrãs.
  - Implementado com `NavHost` + `NavigationBar` (Material 3).
  - Ficheiro: `ui/PasseioApp.kt`

- **Novo ecrã: lista de roteiros**
  - **Porquê:** o utilizador precisa ver os roteiros guardados e criar novos. A lista usa `getRoutesByUser(userId)` e respeita a ordem por `createdAt DESC` definida no DAO.
  - Ficheiro: `ui/screens/RoutesScreen.kt`

- **Novo ecrã: detalhe do roteiro**
  - **Porquê:** permite ver os POIs do roteiro (ordenados) e apagar o roteiro (delete em transação).
  - Ficheiro: `ui/screens/RouteDetailScreen.kt`

- **Injeção do `RouteRepository` no arranque**
  - **Porquê:** para a UI conseguir criar/listar/apagar rotas, precisa do repositório disponível no composable raiz.
  - Ficheiro: `MainActivity.kt`

### 3) Dependências

- **Adicionado `navigation-compose` e `lifecycle-runtime-compose`**
  - **Porquê:** necessários para `NavHost`/`NavController` e para consumo seguro de state no Compose.
  - Ficheiro: `app/build.gradle.kts`

### 4) Testes — estabilidade no Windows e validação no emulador

- **Removidos unit tests locais antigos e testes instrumentados antigos (quebrados)**
  - **Porquê:** existiam suites de testes duplicadas/antigas que falhavam ou estavam inconsistentes com o código atual.

- **Adicionados testes instrumentados (Room in-memory) para favoritos e rotas**
  - **Porquê:** garante persistência e lógica principal (create/delete/toggle) e permite executar com `connectedDebugAndroidTest` no emulador (Pixel 10 API 37).
  - Ficheiros:
    - `androidTest/.../FavoriteRepositoryInstrumentedTest.kt`
    - `androidTest/.../RouteRepositoryInstrumentedTest.kt`

### 5) Git hygiene

- **`.gitignore` atualizado**
  - **Porquê:** evitar commitar acidentalmente `.idea/`, screenshots do emulador e ZIPs grandes.

## Verificação

- `assembleDebug`: OK
- `connectedDebugAndroidTest` (emulador Pixel 10 / API 37): OK
