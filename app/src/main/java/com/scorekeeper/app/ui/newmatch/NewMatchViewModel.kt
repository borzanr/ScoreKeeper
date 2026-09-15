package com.scorekeeper.app.ui.newmatch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scorekeeper.app.data.MatchRepository
import com.scorekeeper.app.rules.EndType
import com.scorekeeper.app.rules.GameTemplate
import com.scorekeeper.app.rules.Templates
import com.scorekeeper.app.rules.TurnOrder
import com.scorekeeper.app.rules.WinCondition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class NewMatchError { MIN_PLAYERS, MAX_PLAYERS, ROUNDS_REQUIRED }

data class NewMatchUiState(
    val template: GameTemplate = Templates.FREE,
    val lowestWins: Boolean = Templates.FREE.win == WinCondition.LOWEST,
    val targetText: String = "",
    val nameInput: String = "",
    val names: List<String> = emptyList(),
    val error: NewMatchError? = null,
    val starter: String? = null,
    val creating: Boolean = false,
)

class NewMatchViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = MatchRepository.from(app)

    private val _state = MutableStateFlow(NewMatchUiState())
    val state: StateFlow<NewMatchUiState> = _state.asStateFlow()

    val recentPlayers: StateFlow<List<String>> = repository.observeRecentPlayerNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectTemplate(template: GameTemplate) = _state.update {
        it.copy(
            template = template,
            lowestWins = template.win == WinCondition.LOWEST,
            targetText = template.defaultTarget?.toString().orEmpty(),
            error = null,
        )
    }

    fun setLowestWins(lowestWins: Boolean) = _state.update { it.copy(lowestWins = lowestWins) }

    fun setTarget(text: String) = _state.update {
        it.copy(targetText = text.filter(Char::isDigit).take(MAX_TARGET_DIGITS), error = null)
    }

    fun setNameInput(text: String) = _state.update { it.copy(nameInput = text.take(MAX_NAME_LENGTH)) }

    fun addTypedName() {
        val name = _state.value.nameInput.trim()
        if (name.isEmpty()) return
        addName(name)
        _state.update { it.copy(nameInput = "") }
    }

    fun toggleRecent(name: String) {
        val index = _state.value.names.indexOfFirst { it.equals(name, ignoreCase = true) }
        if (index >= 0) removeName(index) else addName(name)
    }

    fun removeName(index: Int) = _state.update { state ->
        state.copy(names = state.names.filterIndexed { i, _ -> i != index }, error = null)
    }

    fun pickStarter(random: Random = Random.Default) = _state.update { state ->
        if (state.names.size < 2) return@update state
        val rotated = TurnOrder.randomStart(state.names, random)
        state.copy(names = rotated, starter = rotated.first())
    }

    fun dismissStarter() = _state.update { it.copy(starter = null) }

    fun start(onCreated: (Long) -> Unit) {
        val state = _state.value
        if (state.creating) return

        val template = state.template
        val target = state.targetText.toIntOrNull()?.takeIf { it > 0 }
        val error = when {
            state.names.size < template.minPlayers -> NewMatchError.MIN_PLAYERS
            state.names.size > template.maxPlayers -> NewMatchError.MAX_PLAYERS
            template.endType == EndType.ROUNDS && target == null -> NewMatchError.ROUNDS_REQUIRED
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(error = error) }
            return
        }

        _state.update { it.copy(creating = true) }
        viewModelScope.launch {
            val matchId = repository.createMatch(
                template = template,
                lowestWins = state.lowestWins,
                target = if (template.targetEditable) target else null,
                names = state.names,
            )
            onCreated(matchId)
        }
    }

    private fun addName(name: String) = _state.update { state ->
        val duplicate = state.names.any { it.equals(name, ignoreCase = true) }
        if (duplicate) state else state.copy(names = state.names + name, error = null)
    }

    private companion object {
        const val MAX_NAME_LENGTH = 20
        const val MAX_TARGET_DIGITS = 5
    }
}
