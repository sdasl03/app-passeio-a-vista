# Relatório da Tarefa RS03 — Edição Offline e Sincronização

## Desenvolvedor
Bruno

## Estado
Em progresso

## Story Points
7

## Requisito
RS03

## Prioridade
Média

## Descrição da funcionalidade
Foi implementada a base da funcionalidade que permite ao utilizador editar favoritos e roteiros offline, mantendo as alterações guardadas localmente para sincronização automática quando a ligação à internet for restabelecida.

## Componentes implementados
1. Base de dados local Room;
2. Entidade de favoritos/roteiros;
3. Entidade de alterações pendentes;
4. DAO para favoritos/roteiros;
5. DAO para fila de sincronização;
6. Repositório para gerir operações offline;
7. WorkManager para executar sincronização em segundo plano;
8. Indicador visual de estado na interface.

## Fluxo funcional
1. O utilizador adiciona ou edita um favorito/roteiro;
2. A alteração é guardada localmente;
3. A alteração é registada na fila de pendências;
4. O WorkManager agenda a sincronização;
5. Quando houver internet, o Worker processa as pendências;
6. O item passa para estado sincronizado.

## Estado actual
A funcionalidade está em progresso, com a base técnica concluída. A sincronização real com API externa ainda deverá ser finalizada após disponibilidade do backend.

## Testes previstos
- Testar criação offline;
- Testar edição offline;
- Testar persistência após reiniciar a aplicação;
- Testar sincronização com internet;
- Testar reprocessamento em caso de falha.

## Por fazer
- Finalizar WorkManager com chamada real à API;
- Testar sincronização online;
- Implementar tratamento de conflitos;
- Melhorar logs de sincronização.
