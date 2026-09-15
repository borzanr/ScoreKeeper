package com.scorekeeper.app.ui.intro

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.scorekeeper.app.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val FADE_IN_MS = 450
private const val HOLD_MS = 1_400L

/**
 * Abertura com a ilustração do mago. Curta e pulável com um toque: é um app de
 * utilidade, ninguém quer esperar para marcar ponto.
 */
@Composable
fun IntroScreen(onFinished: () -> Unit) {
    val finish by rememberUpdatedState(onFinished)
    val alpha = remember { Animatable(0f) }
    val zoom = remember { Animatable(1.06f) }

    LaunchedEffect(Unit) {
        launch { zoom.animateTo(1f, tween(FADE_IN_MS + HOLD_MS.toInt())) }
        alpha.animateTo(1f, tween(FADE_IN_MS))
        delay(HOLD_MS)
        finish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { finish() },
    ) {
        // A arte é paisagem: em pé ela cabe inteira na largura, centralizada,
        // com o fundo escuro se fundindo às bordas por um degradê.
        Image(
            painter = painterResource(R.drawable.intro_wizard),
            contentDescription = stringResource(R.string.app_name),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha.value)
                .scale(zoom.value),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to MaterialTheme.colorScheme.background,
                        0.3f to Color.Transparent,
                        0.7f to Color.Transparent,
                        1f to MaterialTheme.colorScheme.background,
                    )
                ),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(horizontal = 32.dp, vertical = 40.dp)
                .alpha(alpha.value),
        ) {
            Text(
                text = stringResource(R.string.intro_tagline),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.intro_tap_to_skip),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
