package com.scorekeeper.app

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.MobileAds
import com.scorekeeper.app.ads.AdIds
import com.scorekeeper.app.ads.ConsentManager
import com.scorekeeper.app.ads.InterstitialManager
import com.scorekeeper.app.ui.AppLanguage
import com.scorekeeper.app.ui.ScoreKeeperApp
import com.scorekeeper.app.ui.theme.ScoreKeeperTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    // Aplica o idioma escolhido em Ajustes antes de qualquer recurso ser lido.
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLanguage.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // O app é sempre escuro, independente do tema do sistema: as barras
        // precisam ser forçadas a dark, senão a de navegação fica branca.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        // Consentimento primeiro: na Europa é proibido carregar anúncio antes de
        // perguntar. Fora de lá o callback vem quase imediatamente.
        ConsentManager.gather(this) {
            lifecycleScope.launch {
                // A inicialização do SDK faz I/O de disco: sai da main thread para
                // não atrasar o primeiro frame.
                withContext(Dispatchers.IO) { MobileAds.initialize(this@MainActivity) }
                AdIds.applyTestDevices()
                // Já o load do interstitial EXIGE a main thread (checkMainThread no SDK).
                InterstitialManager.preload(applicationContext)
            }
        }

        setContent {
            ScoreKeeperTheme {
                ScoreKeeperApp()
            }
        }
    }
}
