package com.scorekeeper.app.ui.match

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.scorekeeper.app.data.AppPrefs
import com.scorekeeper.app.data.MatchRepository
import com.scorekeeper.app.data.MatchSnapshot
import com.scorekeeper.app.rules.CellKey
import com.scorekeeper.app.rules.Entry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MatchUiState(
    val match: MatchSnapshot,
    val canUndo: Boolean,
    val showResult: Boolean,
    val showGuide: Boolean,
)

/** O que fazer depois de encerrar: anúncio, convite para avaliar, ou nada. */
data class FinishOutcome(val showAd: Boolean, val askReview: Boolean)

class MatchViewModel(app: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(app) {

    private val matchId: Long = checkNotNull(savedStateHandle.get<Long>("matchId"))
    private val repository = MatchRepository.from(app)
    private val prefs = AppPrefs(app)

    /** Cada item guarda o estado anterior das células que uma ação mudou. */
    private val undoStack = ArrayDeque<List<Pair<CellKey, Entry?>>>()
    private val undoDepth = MutableStateFlow(0)

    private val manualResult = MutableStateFlow(false)

    /**
     * Placar no momento em que o resultado automático foi fechado. Enquanto nada
     * mudar, ele não reabre sozinho; se a pessoa corrigir uma célula e o jogo
     * continuar terminado, reabre.
     */
    private val dismissedEntriesHash = MutableStateFlow<Int?>(null)

    private val guideVisible = MutableStateFlow(false)

    val state: StateFlow<MatchUiState?> = combine(
        repository.observeMatch(matchId),
        undoDepth,
        manualResult,
        dismissedEntriesHash,
        guideVisible,
    ) { match, depth, manual, dismissedHash, guide ->
        match?.let {
            val autoShow = it.reachedEnd && !it.isFinalized && dismissedHash != it.entries.hashCode()
            MatchUiState(match = it, canUndo = depth > 0, showResult = manual || autoShow, showGuide = guide)
        }
    }.stateIn<MatchUiState?>(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        // O guia abre sozinho só na primeira partida de cada tipo de jogo, e só
        // antes do primeiro ponto; depois fica no botão "?".
        viewModelScope.launch {
            val match = repository.observeMatch(matchId).first() ?: return@launch
            if (match.entries.isEmpty() && prefs.markGuideSeen(match.template.id)) {
                guideVisible.value = true
            }
        }
    }

    fun showGuide() {
        guideVisible.value = true
    }

    fun dismissGuide() {
        guideVisible.value = false
    }

    fun save(entry: Entry) {
        val match = state.value?.match ?: return
        val cell = CellKey(entry.playerId, entry.round)
        val before = mutableListOf(cell to match.entryAt(entry.playerId, entry.round))
        val toSave = mutableListOf(entry)

        // Só uma pessoa encerra a rodada: marcar alguém desmarca quem estava marcado.
        if (entry.detail.closer) {
            match.entries
                .filter { it.round == entry.round && it.playerId != entry.playerId && it.detail.closer }
                .forEach { other ->
                    before += CellKey(other.playerId, other.round) to other
                    toSave += other.copy(detail = other.detail.copy(closer = false))
                }
        }

        pushUndo(before)
        viewModelScope.launch { repository.saveEntries(matchId, toSave) }
    }

    fun delete(cell: CellKey) {
        val before = state.value?.match?.entryAt(cell.playerId, cell.round) ?: return
        pushUndo(listOf(cell to before))
        viewModelScope.launch { repository.deleteEntry(matchId, cell) }
    }

    fun undo() {
        val change = undoStack.removeLastOrNull() ?: return
        undoDepth.value = undoStack.size
        viewModelScope.launch { repository.restore(matchId, change) }
    }

    fun showResult() {
        manualResult.value = true
    }

    fun dismissResult() {
        manualResult.value = false
        dismissedEntriesHash.value = state.value?.match?.entries?.hashCode()
    }

    /** Fecha o resultado e, se a partida já estava encerrada, reabre para continuar. */
    fun keepPlaying() {
        val match = state.value?.match
        dismissResult()
        if (match?.isFinalized == true) {
            viewModelScope.launch { repository.setFinished(matchId, false) }
        }
    }

    fun finish(onDone: (FinishOutcome) -> Unit) {
        val match = state.value?.match ?: return
        viewModelScope.launch {
            // Reabrir e encerrar de novo a mesma partida não conta de novo.
            if (match.isFinalized) {
                onDone(FinishOutcome(showAd = false, askReview = false))
                return@launch
            }
            repository.setFinished(matchId, true)
            val finishedCount = prefs.registerFinishedMatch()
            val showAd = finishedCount % AD_EVERY_FINISHED_MATCHES == 0
            val askReview = !showAd && finishedCount >= REVIEW_AFTER_FINISHED_MATCHES && !prefs.reviewAsked()
            if (askReview) prefs.markReviewAsked()
            onDone(FinishOutcome(showAd, askReview))
        }
    }

    private fun pushUndo(change: List<Pair<CellKey, Entry?>>) {
        undoStack.addLast(change)
        if (undoStack.size > MAX_UNDO) undoStack.removeFirst()
        undoDepth.value = undoStack.size
    }

    private companion object {
        const val MAX_UNDO = 30

        // Nunca na primeira partida, e no máximo uma a cada três.
        const val AD_EVERY_FINISHED_MATCHES = 3
        const val REVIEW_AFTER_FINISHED_MATCHES = 2
    }
}
