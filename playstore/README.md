# Assets da Google Play

As imagens são geradas a partir dos HTMLs em `src/` com Chrome headless.
Para regerar depois de editar um HTML, veja [Regerar](#regerar).

## Arquivos para upload

| Arquivo | Tamanho | Onde vai |
| --- | --- | --- |
| `icon-512.png` | 512×512 | Ícone do app (a Play arredonda os cantos sozinha) |
| `feature-graphic-1024x500.png` | 1024×500 | Gráfico de destaque **pt-BR** (obrigatório) |
| `feature-graphic-1024x500-en.png` | 1024×500 | Gráfico de destaque **en-US** |
| `feature-graphic-1024x500-es.png` | 1024×500 | Gráfico de destaque **es-ES / es-419** |
| `screenshots/01.png` … `05.png` | 1080×1920 | Capturas da ficha **pt-BR** |
| `screenshots-en/01.png` … `05.png` | 1080×1920 | Capturas da ficha **en-US** |
| `screenshots-es/01.png` … `05.png` | 1080×1920 | Capturas da ficha **es-ES / es-419** |
| `data_safety.csv` | — | Importar em **Segurança dos dados** |

**Política de privacidade:** `https://scorekeeper.borzanti.com/privacidade/`.
O conteúdo está em `../site/`, no mesmo esquema do LuckyDraw:
- publique a pasta num repositório com GitHub Pages;
- aponte o subdomínio `scorekeeper` para ele;
- o arquivo `CNAME` já está pronto.

**Sobre as capturas:**
- São telas reais do app release rodando num Redmi Note 9S, com jogadores fictícios.
- Nas telas usadas não aparece anúncio: a partida não tem banner.

## Identidade

- **Cores:**
  - fundo `#0E0A14`;
  - dourado `#F2C14E` (ação e brasão);
  - lilás `#B794F6` (manto do mago).
- **Arte:** `mago-keeper-logo.jfif`, na raiz do projeto. **Gerada por IA.**
  - O ícone e a splash são o brasão recortado dela.
  - O gráfico de destaque usa a ilustração inteira.

### Declaração de recursos gerados com IA

A Play pergunta isso a cada versão nova. A resposta é sempre a mesma:

| Recurso | Marcar? | Por quê |
| --- | --- | --- |
| Ícone do app | Sim | Recorte da ilustração gerada por IA |
| Recursos gráficos | Sim | Usam a mesma ilustração |
| Capturas de tela | Não | Telas reais do app; a moldura e as frases são HTML e CSS |

## Nomes de jogos: o que pode e o que não pode

Não cite jogos de marca registrada no título, no ícone, nas capturas nem na
descrição. Isso inclui Skyjo, Flip 7, Yahtzee, Uno e similares.

A política de metadados da Play e o risco de reclamação de propriedade intelectual não
compensam. Os modelos têm nomes genéricos de propósito: "grade de cartas", "arrisque a
sorte", "cartela de dados".

Jogos tradicionais sem dono podem ser citados à vontade: truco, dominó, rummy, buraco,
canastra, golf de cartas, general, pôquer de dados.

## Textos da ficha (PT-BR)

**Título** (máx. 30). É também o nome do app na Play Console.

```
Score Keeper: Placar de Jogos
```

**Descrição curta** (máx. 80)

```
Placar para truco, dominó, cartas e dados. Soma, ranking e vencedor na hora.
```

**Descrição longa**

```
Chega de papel e caneta na mesa. O Score Keeper marca os pontos de qualquer jogo, soma
tudo sozinho e mostra quem está ganhando a cada rodada.

JOGOS PRONTOS
• Truco (até 12): mão normal, truco, seis, nove e doze em um toque
• Dominó por batidas: simples, carroça, lá-e-lô e cruzada
• Dominó por soma das pedras
• Jogos de cartas em grade, com a regra de dobrar de quem fecha a rodada
• Jogos de virar cartas e arriscar a sorte, com bônus, ×2 e estouro
• Cartela de dados com 13 categorias e bônus automático
• Golf de cartas, rummy e buraco por pontos
• Placar livre para qualquer outro jogo, com vitória do maior ou do menor total

TOQUE E PRONTO
Toque na célula do jogador e lance os pontos. Nos jogos de cartas você toca nas cartas e o
app soma. Na cartela de dados você escolhe os dados e ele calcula a categoria. Ninguém
precisa lançar zero.

FIM DE JOGO AUTOMÁTICO
Defina o alvo (100 pontos, 12 tentos, 9 rodadas) e o app avisa quando alguém chega lá,
com pódio e confete. Compartilhe o resultado com a turma no WhatsApp.

ERROU? CORRIGE
Toque em qualquer célula para editar ou apagar. O botão desfazer volta a última
mudança.

GUIA RÁPIDO EM CADA JOGO
Na dúvida de como marcar? Cada jogo tem um guia curto dentro do app.

FEITO PARA A MESA
• A tela não apaga durante a partida
• Sem anúncios durante o jogo
• Partidas salvas: feche o app e continue depois
• Funciona offline, sem cadastro e sem login
• Português, inglês e espanhol

PARA QUEM
Noite de jogos com os amigos, truco com a família, dominó na praça, campeonato de
cartas, jogos de tabuleiro, torneios em casa e qualquer mesa onde alguém sempre esquece
quanto estava o placar.
```

## Textos da ficha (EN)

**Title** (máx. 30)

```
Score Keeper: Game Scoreboard
```

**Descrição curta** (máx. 80)

```
Score keeper for card, dice and board games. Totals, ranking and winner.
```

**Descrição longa**

```
Ditch the pen and paper. Score Keeper tracks the points of any game, adds everything up
for you and shows who is winning after every round.

READY-MADE GAMES
• Grid card games, including the double-score rule for whoever ends the round
• Flip-and-bust card games with bonus cards, ×2 and busts
• Dice scorecard with all 13 categories and the upper-section bonus
• Golf (card game) and rummy penalty points
• Dominoes by pip count, or by Brazilian-style hits
• Truco, with regular, truco, six, nine and twelve in one tap
• Free scoring for any other game, highest or lowest total wins

TAP AND DONE
Tap a player's cell and enter the points. For card games, tap the cards and the app adds
them up. For the dice scorecard, pick the dice and it scores the category. No need to type
zeros for players who did not score.

AUTOMATIC GAME OVER
Set the target (100 points, 12, 9 rounds) and the app tells you when someone gets there,
with a podium and confetti. Share the result with your group.

MADE A MISTAKE? FIX IT
Tap any cell to edit or delete it. Undo reverts the last change.

QUICK GUIDE FOR EVERY GAME
Not sure how to score? Every game has a short guide inside the app.

BUILT FOR THE TABLE
• The screen stays on during the match
• No ads while you play
• Matches are saved: close the app and continue later
• Works offline, no sign-up, no login
• English, Portuguese and Spanish

GREAT FOR
Game nights with friends, family card games, board game groups, dice games, home
tournaments and every table where someone always forgets the score.
```

## Textos da ficha (ES)

Espanhol neutro, para a Espanha e a América Latina. "Anotador" é como boa parte da
América Latina chama o app de marcar pontos, e "marcador" é o termo da Espanha. Os dois
aparecem na descrição de propósito.

**Título** (máx. 30)

```
Score Keeper: Anotador Juegos
```

**Descrição curta** (máx. 80)

```
Anotador de puntos para truco, dominó, cartas y dados. Totales y ganador.
```

**Descrição longa**

```
Olvídate del papel y el lápiz. Score Keeper es el anotador y marcador de puntos para
cualquier juego: suma todo solo y muestra quién va ganando en cada ronda.

JUEGOS LISTOS
• Truco (hasta 12): normal, truco, seis, nueve y doce en un toque
• Dominó por cierres o por suma de puntos
• Juegos de cartas en cuadrícula, con la regla de duplicar a quien cierra la ronda
• Juegos de voltear cartas y tentar la suerte, con bonificaciones, ×2 y pasarse
• Hoja de dados con 13 categorías y bonus automático
• Golf de cartas y rummy por penalización
• Marcador libre para cualquier otro juego, gana el total más alto o el más bajo

UN TOQUE Y LISTO
Toca la celda del jugador y anota los puntos. En los juegos de cartas tocas las cartas y la
app suma. En la hoja de dados eliges los dados y calcula la categoría. No hace falta anotar
ceros.

FIN DE PARTIDA AUTOMÁTICO
Define el objetivo (100 puntos, 12, 9 rondas) y la app avisa cuando alguien llega, con
podio y confeti. Comparte el resultado con tu grupo.

¿TE EQUIVOCASTE? CORRÍGELO
Toca cualquier celda para editarla o borrarla. Deshacer revierte el último cambio.

GUÍA RÁPIDA EN CADA JUEGO
¿No sabes cómo anotar? Cada juego tiene una guía corta dentro de la app.

HECHO PARA LA MESA
• La pantalla no se apaga durante la partida
• Sin anuncios mientras juegas
• Partidas guardadas: cierra la app y sigue después
• Funciona sin conexión, sin registro y sin inicio de sesión
• Español, portugués e inglés

PARA QUIÉN
Noches de juegos con amigos, truco en familia, dominó, juegos de mesa, torneos en casa y
cualquier mesa donde alguien siempre olvida cómo iba el marcador.
```

## Checklist de publicação na Play Console

A ordem abaixo evita retrabalho.

### 1. Antes de criar o app
- [x] Nome definido: **Score Keeper: Placar de Jogos**. No aparelho, o ícone mostra só "Score Keeper".
- [ ] App criado no **AdMob**. App ID e blocos reais colados em `AndroidManifest.xml` e `ads/AdIds.kt`.
- [ ] Chave de upload criada e `keystore.properties` preenchido.
- [ ] Site publicado. `https://scorekeeper.borzanti.com/privacidade/` abre numa janela anônima.

### 2. Criar o app
Caminho: **Todos os apps → Criar app**.
- Nome do app: `Score Keeper: Placar de Jogos`.
- Idioma padrão **português (Brasil)**. Não dá para trocar depois.
- Tipo **App**, **Gratuito**.

### 3. Configuração do app

| Item | Resposta |
| --- | --- |
| Política de privacidade | `https://scorekeeper.borzanti.com/privacidade/` |
| Acesso ao app | Todo o conteúdo é livre, sem login |
| Anúncios | **Sim, contém anúncios** |
| ID de publicidade | Sim, usado por anúncios (AdMob) |
| Público-alvo | 13+. Não marcar faixas infantis: o app tem anúncios. |
| App de notícias / COVID-19 / governo | Não |

### 4. Classificação de conteúdo
⚠️ No questionário, **jogos de azar = NÃO**.
- O app só anota pontos. Não tem apostas, dinheiro, prêmios nem simulação de cassino.
- Truco e dominó aparecem como jogos de mesa, e o app não joga as cartas.
- Responder "sim" empurra a classificação para 18+ sem motivo.

### 5. Segurança dos dados
Importe `data_safety.csv`. As respostas são as mesmas do LuckyDraw, porque o SDK é o mesmo:
- **Coleta de dados:** sim, pelo SDK de anúncios.
- **Tipo de dado:** ID de publicidade.
- **Finalidade:** publicidade.
- **Uso:** obrigatório.
- **Compartilhamento:** sim, com o Google AdMob.
- **Criptografia em trânsito:** sim.
- **Exclusão:** sim, redefinindo o ID de publicidade.

Partidas e nomes **não** entram no formulário, porque nunca saem do aparelho.

### 6. Fichas da loja (uma por idioma)

| Idioma | Textos | Capturas |
| --- | --- | --- |
| Português (Brasil) | seção PT-BR | `screenshots/` + `feature-graphic-1024x500.png` |
| Inglês (EUA) | seção EN | `screenshots-en/` + `feature-graphic-1024x500-en.png` |
| Espanhol (Espanha e América Latina) | seção ES | `screenshots-es/` + `feature-graphic-1024x500-es.png` |

Categoria: **Jogos de tabuleiro**, não **Ferramentas**.
- Quem procura "marcador de truco" ou "score keeper" navega pelos jogos.
- Os concorrentes diretos estão nessa categoria.

### 7. Países
Todos os países. O app cai no inglês em qualquer idioma que não seja PT ou ES.
- O consentimento da UMP (a plataforma de consentimento do Google) cobre GDPR na Europa e no Reino Unido.
- Ajustes → Opções de privacidade permite rever a escolha, como a UE exige.

### 8. Primeira versão
1. Comece por **Teste interno**.
2. Confirme que instala e abre pela Play.
3. Promova para **Produção**.

- **Arquivo para upload:** `app/build/outputs/bundle/release/app-release.aab`, gerado com `./gradlew.bat bundleRelease`.
- **Assinatura:** aceite o **Play App Signing**.
- **A cada nova versão:** suba o `versionCode` em `app/build.gradle.kts`.

## Regerar

```bash
bash playstore/render.sh
```

Gera o gráfico de destaque e as capturas nos três idiomas.

**Onde editar:**
- Títulos e recortes de cada slide: objeto `SLIDES` em `src/shot.html`.
- Textos do gráfico de destaque: objeto `COPY` em `src/feature.html`.
- Ícone: gerado a partir da arte (o script está fora do repositório). O PNG já está em `icon-512.png`.

**Atualizar as telas do app:**
1. Capture com `adb shell screencap` e `adb pull`. Não use `>` no PowerShell, porque ele corrompe o PNG.
2. Troque os arquivos em `src/shots/` (português), `src/shots-en/` (inglês) ou `src/shots-es/` (espanhol).
3. Os nomes dos arquivos são: `scoreboard`, `cards`, `dice`, `result`, `templates`.

Para capturar em outro idioma, use **Ajustes → Idioma** dentro do app.
