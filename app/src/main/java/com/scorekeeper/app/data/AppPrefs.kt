package com.scorekeeper.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "scorekeeper")

/** Preferências pequenas; partidas e placares ficam no Room. */
class AppPrefs(context: Context) {

    private val store = context.applicationContext.dataStore

    /** Conta mais uma partida encerrada e devolve o novo total. */
    suspend fun registerFinishedMatch(): Int {
        var count = 0
        store.edit { prefs ->
            count = (prefs[FINISHED_MATCHES] ?: 0) + 1
            prefs[FINISHED_MATCHES] = count
        }
        return count
    }

    suspend fun reviewAsked(): Boolean = store.data.first()[REVIEW_ASKED] ?: false

    suspend fun markReviewAsked() {
        store.edit { it[REVIEW_ASKED] = true }
    }

    /** Marca o guia do modelo como visto. Devolve `true` se era a primeira vez. */
    suspend fun markGuideSeen(templateId: String): Boolean {
        var firstTime = false
        store.edit { prefs ->
            val seen = prefs[GUIDES_SEEN].orEmpty()
            firstTime = templateId !in seen
            if (firstTime) prefs[GUIDES_SEEN] = seen + templateId
        }
        return firstTime
    }

    private companion object {
        val FINISHED_MATCHES = intPreferencesKey("finished_matches")
        val REVIEW_ASKED = booleanPreferencesKey("review_asked")
        val GUIDES_SEEN = stringSetPreferencesKey("guides_seen")
    }
}
