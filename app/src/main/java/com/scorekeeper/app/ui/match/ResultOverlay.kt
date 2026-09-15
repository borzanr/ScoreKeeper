package com.scorekeeper.app.ui.match

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scorekeeper.app.R
import com.scorekeeper.app.data.MatchSnapshot
import com.scorekeeper.app.rules.Standing
import com.scorekeeper.app.ui.components.PlayerDot
import com.scorekeeper.app.ui.components.PrimaryButton
import com.scorekeeper.app.ui.theme.ConfettiColors
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

fun medal(rank: Int): String = when (rank) {
    1 -> "🥇"
    2 -> "🥈"
    3 -> "🥉"
    else -> "$rank."
}

/** Pódio no fim da partida, com confete — o momento de mostrar para a mesa. */
@Composable
fun ResultOverlay(
    match: MatchSnapshot,
    onShare: () -> Unit,
    onFinish: () -> Unit,
    onKeepPlaying: () -> Unit,
    onTryLuckyDraw: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val winners = match.standings.filter { it.rank == 1 }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.97f))
            .pointerInput(Unit) {}
            .systemBarsPadding(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            Text(
                text = stringResource(if (winners.size > 1) R.string.result_tie else R.string.result_winner),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = winners.joinToString(" · ") { match.playerName(it.playerId) },
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 44.sp, lineHeight = 50.sp),
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                match.standings.forEach { standing -> StandingRow(match, standing) }
            }

            OutlinedButton(onClick = onShare) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Text(stringResource(R.string.action_share), modifier = Modifier.padding(start = 8.dp))
            }

            PrimaryButton(
                text = stringResource(if (match.isFinalized) R.string.result_back_home else R.string.result_finish),
                onClick = onFinish,
            )
            TextButton(onClick = onKeepPlaying) {
                Text(stringResource(R.string.result_keep_playing))
            }
            TextButton(onClick = onTryLuckyDraw) {
                Text(
                    text = stringResource(R.string.result_luckydraw),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }

        KonfettiView(
            modifier = Modifier.fillMaxSize(),
            parties = remember { confettiParties() },
        )
    }
}

@Composable
private fun StandingRow(match: MatchSnapshot, standing: Standing) {
    val player = match.players.firstOrNull { it.id == standing.playerId }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = medal(standing.rank),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.width(32.dp),
            )
            PlayerDot(seat = player?.seat ?: 0)
            Text(
                text = player?.name.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = standing.total.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = if (standing.rank == 1) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun confettiParties(): List<Party> {
    val emitter = Emitter(duration = 220, TimeUnit.MILLISECONDS).max(220)
    return listOf(
        Party(
            speed = 12f,
            maxSpeed = 42f,
            damping = 0.9f,
            angle = 270,
            spread = 90,
            colors = ConfettiColors,
            position = Position.Relative(0.5, 0.0),
            emitter = emitter,
        ),
    )
}
