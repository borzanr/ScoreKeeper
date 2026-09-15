package com.scorekeeper.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scorekeeper.app.data.MatchRepository
import com.scorekeeper.app.data.MatchSnapshot
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = MatchRepository.from(app)

    /** `null` enquanto o banco ainda não respondeu, para não piscar o estado vazio. */
    val matches: StateFlow<List<MatchSnapshot>?> = repository.observeMatches()
        .stateIn<List<MatchSnapshot>?>(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete(matchId: Long) {
        viewModelScope.launch { repository.deleteMatch(matchId) }
    }
}
