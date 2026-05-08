# Commit (RF02 + RF07)

## Implementações

### RF02 — Integração com API de Mapa (US002)
- Integração de mapa gratuita usando **OSMDroid + OpenStreetMap** (sem API key).
- Ecrã de mapa em Compose (AndroidView) com marcadores dos POIs existentes na Room.
- Bottom sheet com detalhes do POI ao tocar num marcador.
- Permissões adicionadas: Internet/rede e localização (opcional, para “minha localização”).

Ficheiros principais:
- `app/build.gradle.kts` (dependência OSMDroid)
- `app/src/main/AndroidManifest.xml` (permissões)
- `app/src/main/java/.../ui/screens/MapScreen.kt` (mapa + UI)

### RF07 — Favoritos (Story Points: 8)
- Toggle **Adicionar/Remover favorito** na ficha do POI (persistência imediata em Room).
- Lista de favoritos ordenada por data de criação (DESC) e estados: vazio, loading e erro.
- Repository com operações `add/remove/toggle` e streams para refletir alterações imediatamente na UI.

Ficheiros principais:
- `app/src/main/java/.../data/dao/FavoriteDao.kt`
- `app/src/main/java/.../data/repositories/FavoriteRepository.kt`
- `app/src/main/java/.../ui/screens/MapScreen.kt` (botão/toggle na ficha do POI)
- `app/src/main/java/.../ui/screens/FavoritesSheet.kt` (lista e estados)
- `app/src/main/java/.../ui/PasseioApp.kt` (top bar + estado da lista)

## Testes
- Teste unitário do repository (toggle) em `app/src/test/.../FavoriteRepositoryTest.kt`.
- Testes instrumentados do DAO (Room) para:
  - Ordenação por data (DESC)
  - Remoção por (userId, poiId)
  em `app/src/androidTest/.../FavoriteDaoInstrumentedTest.kt`.

## Dificuldades
- Em Windows, o projeto está numa pasta com caracteres não-ASCII (ex.: `2º Semestre`, `Aplicações`).
  - O AGP emite aviso/erro conhecido com paths não-ASCII, mitigado com `android.overridePathCheck=true`.
  - A execução de **unit tests locais** pode falhar neste tipo de path; quando acontecer, a solução mais robusta é mover o projeto para um caminho ASCII (ex.: `C:\Work\app-passeio-a-vista`).

## Conclusões
- A app ficou com integração de mapa funcional sem custos/keys e uma UX coerente com Material 3.
- A gestão de favoritos ficou completa (toggle + listagem ordenada + estados) e persistida em Room.
- A base de testes cobre a lógica e a persistência; a execução pode depender do ambiente (sobretudo paths no Windows).
