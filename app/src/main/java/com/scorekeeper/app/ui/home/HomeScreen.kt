package com.scorekeeper.app.ui.home

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scorekeeper.app.R
import com.scorekeeper.app.ads.BannerAd
import com.scorekeeper.app.data.MatchSnapshot
import com.scorekeeper.app.ui.components.SectionTitle
import com.scorekeeper.app.ui.nameRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNewMatch: () -> Unit,
    onOpenMatch: (Long) -> Unit,
    onSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val matches by viewModel.matches.collectAsState()
    var pendingDelete by remember { mutableStateOf<MatchSnapshot?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewMatch,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.home_new_match), style = MaterialTheme.typography.labelLarge) },
            )
        },
        // navigationBarsPadding: com edge-to-edge o Scaffold não recua a bottomBar
        // sozinho, e o banner acabaria por cima dos botões do sistema.
        bottomBar = { BannerAd(Modifier.navigationBarsPadding()) },
    ) { padding ->
        val list = matches
        when {
            list == null -> Unit
            list.isEmpty() -> EmptyState(Modifier.padding(padding))
            else -> MatchList(
                matches = list,
                contentPadding = padding,
                onOpen = onOpenMatch,
                onDelete = { pendingDelete = it },
            )
        }
    }

    pendingDelete?.let { match ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.home_delete_title)) },
            text = { Text(stringResource(R.string.home_delete_body)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(match.id)
                    pendingDelete = null
                }) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

@Composable
private fun MatchList(
    matches: List<MatchSnapshot>,
    contentPadding: PaddingValues,
    onOpen: (Long) -> Unit,
    onDelete: (MatchSnapshot) -> Unit,
) {
    val inProgress = matches.filter { !it.isFinalized }
    val finished = matches.filter { it.isFinalized }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding(),
            // Espaço para o botão flutuante não cobrir o último cartão.
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (inProgress.isNotEmpty()) {
            item(key = "header_progress") { SectionTitle(stringResource(R.string.home_in_progress)) }
            items(inProgress, key = { it.id }) { match ->
                MatchCard(match, onClick = { onOpen(match.id) }, onDelete = { onDelete(match) })
            }
        }
        if (finished.isNotEmpty()) {
            item(key = "header_finished") { SectionTitle(stringResource(R.string.home_finished)) }
            items(finished, key = { it.id }) { match ->
                MatchCard(match, onClick = { onOpen(match.id) }, onDelete = { onDelete(match) })
            }
        }
    }
}

@Composable
private fun MatchCard(match: MatchSnapshot, onClick: () -> Unit, onDelete: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(match.template.nameRes()),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = match.players.joinToString(", ") { it.name },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = leaderText(match),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = remember(match.updatedAt) {
                    DateUtils.getRelativeTimeSpanString(
                        match.updatedAt,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS,
                    ).toString()
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp),
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.home_delete_match),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun leaderText(match: MatchSnapshot): String {
    if (match.entries.isEmpty()) return stringResource(R.string.home_not_started)
    val leaders = match.standings.filter { it.rank == 1 }
    val names = leaders.joinToString(", ") { match.playerName(it.playerId) }
    return stringResource(R.string.home_leader, names, leaders.first().total)
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp),
            )
            Text(
                text = stringResource(R.string.home_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.home_empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
