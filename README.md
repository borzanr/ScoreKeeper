# Score Keeper

Marcador de pontos para jogos de tabuleiro, cartas e dados. Offline, sem backend,
em inglês, português (BR) e espanhol. A base de build, anúncios e tema veio do
LuckyDraw.

## Estrutura

| Pacote | O que tem |
|---|---|
| `rules/` | Lógica pura (testada em JVM): modelos de jogo, soma, classificação, fim de partida, dados, cartas |
| `data/` | Room (partidas, jogadores, células), DataStore (contadores), repositório |
| `ads/` | AdMob + consentimento UMP. Debug sempre usa IDs de teste |
| `ui/` | Compose + Navigation: abertura, início, nova partida, partida, resultado, ajustes |

Anúncios só na tela inicial (banner) e ao encerrar partida (interstitial, 1 a cada
3). Nunca durante a partida.

## Rodar

```bash
./gradlew.bat testDebugUnitTest
./gradlew.bat installDebug
```

Para zerar o estado no aparelho, use `adb shell pm clear com.borzanti.scorekeeper.debug`.
Não desinstale: no MIUI a reinstalação costuma travar.

## Arte

A abertura e o ícone saem de `mago-keeper-logo.jfif`:

- `res/drawable-nodpi/intro_wizard.jpg`: ilustração da abertura
- `res/mipmap-*/ic_launcher*.png` e `res/drawable-nodpi/splash_shield.png`: brasão recortado
- `playstore/icon-512.png`: ícone da loja

## Antes do primeiro release

1. **AdMob:** criar o app e trocar o App ID em `AndroidManifest.xml` e os unit IDs em `ads/AdIds.kt`.
2. **Assinatura:** criar a chave de upload e o `keystore.properties` (modelo em `keystore.properties.example`).
3. **Nome:** na loja, "Score Keeper: Placar de Jogos"; no aparelho, "Score Keeper" (`app_name`).
4. **Loja:** o material está pronto em `playstore/`, com o checklist completo em `playstore/README.md`:
   - textos da ficha em PT, EN e ES;
   - capturas e gráficos de destaque;
   - CSV do Data Safety.

   Nunca cite jogos de marca registrada no título, no ícone ou na descrição.
5. **Política de privacidade:** a página está em `site/privacidade/`. Publique a pasta `site/` em `scorekeeper.borzanti.com`; o `CNAME` já está pronto.
