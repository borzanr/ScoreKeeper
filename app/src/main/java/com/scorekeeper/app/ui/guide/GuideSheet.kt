package com.scorekeeper.app.ui.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.scorekeeper.app.R
import com.scorekeeper.app.rules.GameTemplate
import com.scorekeeper.app.ui.components.PrimaryButton
import com.scorekeeper.app.ui.components.SectionTitle
import com.scorekeeper.app.ui.guideStepsRes
import com.scorekeeper.app.ui.nameRes

private val TIPS = listOf(
    R.string.guide_tip_edit,
    R.string.guide_tip_undo,
    R.string.guide_tip_results,
    R.string.guide_tip_saved,
)

/**
 * Guia rápido de COMO MARCAR no app, não o livro de regras do jogo: curto para
 * ler com a mesa esperando, e sem copiar regras de jogos com marca registrada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideSheet(template: GameTemplate, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
        ) {
            Column {
                Text(
                    text = stringResource(R.string.guide_title).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(template.nameRes()),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }

            template.guideStepsRes().forEachIndexed { index, step ->
                GuideStep(number = index + 1, text = stringResource(step))
            }

            SectionTitle(stringResource(R.string.guide_tips_title))
            TIPS.forEach { tip ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("•", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = stringResource(tip),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            PrimaryButton(text = stringResource(R.string.guide_got_it), onClick = onDismiss)
        }
    }
}

@Composable
private fun GuideStep(number: Int, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary),
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondary,
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .padding(top = 3.dp),
        )
    }
}
