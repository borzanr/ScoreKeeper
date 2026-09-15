package com.scorekeeper.app.ads

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Banner adaptativo ancorado no rodapé. Só é usado na tela inicial: durante a
 * partida não há anúncio nenhum, para ninguém tocar num banner sem querer
 * enquanto lança pontos — a reclamação mais comum contra apps de placar.
 */
@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    // Antes do consentimento não se carrega anúncio nenhum. Enquanto isso o
    // banner não ocupa espaço.
    val canRequestAds by ConsentManager.canRequestAds.collectAsState()
    if (!canRequestAds && !isPreview) return

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val widthDp = maxWidth.value.toInt().coerceAtLeast(320)
        val adSize = remember(widthDp) { adaptiveSize(context, widthDp) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(adSize.height.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (!isPreview) {
                AndroidView(
                    factory = { ctx ->
                        AdView(ctx).apply {
                            setAdSize(adSize)
                            adUnitId = AdIds.banner
                            loadAd(AdRequest.Builder().build())
                        }
                    },
                    onRelease = { it.destroy() },
                )
            }
        }
    }
}

private fun adaptiveSize(context: Context, widthDp: Int): AdSize =
    AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, widthDp)
