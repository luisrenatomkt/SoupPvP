# Soup PvP Legit (souplegit) — Minecraft 1.8.9 Forge

Mecânica clássica de Soup PvP **somente em singleplayer**: clique direito manual com sopa de cogumelos
cura **7 pontos (3,5 corações)** na hora, consome 1 sopa e devolve 1 tigela vazia.
Sem AutoSoup, sem macros, sem cliques artificiais, sem pacotes de rede.

> **Estado honesto:** o código foi escrito, mas **não foi compilado nem testado** (o ambiente onde foi gerado não tinha
> internet para baixar Forge/Gradle). Veja "Relatório de testes" abaixo.

## Como funciona (e por que assim)

| Peça | O que faz |
|---|---|
| `ClientInputHandler` | Detecta o **aperto** do botão de usar item (`MouseEvent`/`KeyInputEvent`) com sopa na mão. Um aperto = no máximo 1 pedido. Segurar o botão **não** repete. Ignora o clique se ele vai abrir baú, porta, mesa, aldeão etc. |
| `ClickGate` | Entrega o pedido do cliente ao servidor integrado (mesma JVM). Pedido é consumido uma única vez e expira em 1 s. |
| `SoupHandler` | No tick do jogador (lado servidor) executa o pedido. Também cancela o consumo vanilla da sopa (`PlayerUseItemEvent.Start`) em singleplayer, para não haver cura dupla nem animação de comer. |
| `SoupLogic` | Valida (só sopa, não criativo/espectador), troca sopa por tigela e aplica **ou** cura `min(7, faltante)` com `heal()` **ou**, se a vida estiver cheia, fome via `addStats`. |
| `SingleplayerCheck` | Cliente: `mc.isSingleplayer()` + servidor integrado ativo. Servidor: precisa ser `IntegratedServer`. Fixo, sem opção de desativar. |

**Decisão técnica:** nenhum item vanilla foi substituído e não há mixins/coremods; só eventos do Forge. O clique é detectado no
cliente porque, no 1.8.9, o evento de "clicar no ar" não é confiável no servidor, e é a única forma de distinguir clique novo
de botão segurado. A cura e o inventário são sempre aplicados no lado servidor.

Comportamentos:
- Vida faltando → cura 7 HP e **não** mexe na fome.
- Vida cheia **e** com fome → restaura fome (6 pontos, como a sopa vanilla), sem cura.
- Vida e fome cheias → a sopa **não** é consumida.
- Sopa vanilla não empilha (max 1); se uma pilha existir (comando/outro mod), diminui 1 e coloca a tigela no inventário. Sem espaço → **nada é consumido** e o jogador recebe um aviso.
- Pilha de 1 → a sopa vira tigela no próprio slot.
- Criativo/espectador → sem efeito.
- Em singleplayer, a sopa deixa de poder ser "comida" do jeito vanilla (por fome). Para voltar ao normal: `enabled=false`.

## Configuração (`config/souplegit.cfg`)
- `enabled` (padrão `true`)
- `healAmount` (padrão `7.0`, de 0.5 a 100)
- `foodAmount` (padrão `6`, de 0 a 20; só vale com vida cheia)

## Compilar pelo GitHub (sem instalar nada)
1. Crie um repositório no GitHub e envie **todo** o conteúdo desta pasta (inclusive `.github/workflows/build.yml`).
2. Abra a aba **Actions** → workflow **Build mod** (roda sozinho a cada push; ou clique em *Run workflow*).
3. Quando ficar verde, abra a execução → **Artifacts** → baixe `souplegit-jar` → dentro está `souplegit-1.8.9-1.0.0.jar`.
4. Se ficar vermelho, copie o final do log e me envie.

## Compilar local
Requer JDK 8 e Gradle 2.14.1 (ou um wrapper 2.14.1 seu):
```
gradle setupDecompWorkspace build
```
O jar sai em `build/libs/`.

## Instalar
1. Instale o Minecraft Forge 1.8.9 (11.15.1.2318).
2. Coloque o `.jar` em `.minecraft/mods/`.
3. Abra o jogo com o perfil Forge 1.8.9 e entre em um mundo singleplayer.

## Plano de testes (roteiro manual — NENHUM executado ainda)
| # | Teste | Como verificar | Resultado |
|---|---|---|---|
| 1 | Mod carrega | Lista de mods mostra "Soup PvP Legit" | não executado |
| 2 | Cura exata de 7 | `/give @p mushroom_stew`, `/effect`/dano até 10 HP → clicar → 17 HP | não executado |
| 3 | Nunca passa do máximo | Com 17 HP → clicar → 20 HP (fome inalterada); com 20 HP e fome cheia → sopa preservada | não executado |
| 3b | Fome só com vida cheia | Vida 20 e fome baixa → clicar → fome sobe 6, tigela devolvida; com vida baixa a fome não muda | não executado |
| 4 | Rápido e manual | Cura no mesmo instante do clique, sem barra de comer | não executado |
| 5 | Uma sopa por uso | Segurar o botão com várias sopas na hotbar → só 1 por clique | não executado |
| 6 | Devolve tigela | Slot vira tigela | não executado |
| 7 | Pilhas | `/give @p mushroom_stew 5` → -1 por clique, tigela vai para o inventário | não executado |
| 8 | Sem consumo automático | Ficar parado com vida baixa e sopa na mão: nada acontece | não executado |
| 9 | Outros alimentos | Maçã dourada, pão, poção: comportamento vanilla | não executado |
| 10 | Multiplayer | Entrar em servidor: sopa age como vanilla, sem cura instantânea | não executado |
| 11 | Trocar de mundo | Sair/entrar de mundos várias vezes sem erro no log | não executado |
| 12 | Compila e gera .jar | GitHub Actions verde | não executado |
| + | Inventário cheio com pilha de sopa | Mensagem de aviso e nada consumido | não executado |
| + | Clique em baú/porta com sopa na mão | Interage com o bloco, sem cura | não executado |

## Limitações conhecidas
- **Não compilado/testado** (ver topo). Nomes de API foram escritos de memória do Forge 1.8.9 (11.15.1.2318, mappings `stable_22`); podem exigir pequenos ajustes de compilação.
- Se o mundo for aberto para LAN, o mod continua valendo só para o dono do mundo (outros jogadores não geram pedidos), mas a sessão ainda conta como singleplayer.
- A lista de blocos/entidades que "ganham" o clique (baú, porta, aldeão...) é uma heurística; blocos interativos de outros mods podem receber o clique e a cura ao mesmo tempo.
- Trocar a tecla "usar item" para uma tecla do teclado é suportado, mas só o mouse foi considerado no desenho principal.
