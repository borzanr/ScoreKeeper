package com.scorekeeper.app.data

import android.content.Context
import androidx.room.withTransaction
import com.scorekeeper.app.rules.CellKey
import com.scorekeeper.app.rules.Entry
import com.scorekeeper.app.rules.GameTemplate
import com.scorekeeper.app.rules.Templates
import com.scorekeeper.app.rules.WinCondition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class MatchRepository(
    private val db: AppDatabase,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val dao = db.dao()

    fun observeMatches(): Flow<List<MatchSnapshot>> =
        combine(dao.observeMatches(), dao.observeAllSeats(), dao.observeAllEntries()) { matches, seats, entries ->
            val seatsByMatch = seats.groupBy { it.matchId }
            val entriesByMatch = entries.groupBy { it.matchId }
            matches.map { it.toSnapshot(seatsByMatch[it.id].orEmpty(), entriesByMatch[it.id].orEmpty()) }
        }

    fun observeMatch(matchId: Long): Flow<MatchSnapshot?> =
        combine(dao.observeMatch(matchId), dao.observeSeats(matchId), dao.observeEntries(matchId)) { match, seats, entries ->
            match?.toSnapshot(seats, entries)
        }

    fun observeRecentPlayerNames(): Flow<List<String>> =
        dao.observePlayers().map { players -> players.map { it.name } }

    /** Cria a partida reaproveitando jogadores já salvos com o mesmo nome. */
    suspend fun createMatch(
        template: GameTemplate,
        lowestWins: Boolean,
        target: Int?,
        names: List<String>,
    ): Long = db.withTransaction {
        val now = clock()
        val matchId = dao.insertMatch(
            MatchEntity(
                templateId = template.id,
                lowestWins = lowestWins,
                target = target,
                createdAt = now,
                updatedAt = now,
            )
        )
        val seats = names.mapIndexed { seat, raw ->
            val name = raw.trim()
            val existing = dao.findPlayer(name)
            val playerId = if (existing != null) {
                dao.updatePlayer(existing.copy(lastUsedAt = now))
                existing.id
            } else {
                dao.insertPlayer(PlayerEntity(name = name, lastUsedAt = now))
            }
            MatchPlayerEntity(matchId = matchId, playerId = playerId, seat = seat)
        }
        dao.insertMatchPlayers(seats)
        matchId
    }

    suspend fun saveEntries(matchId: Long, entries: List<Entry>) {
        db.withTransaction {
            entries.forEach { dao.upsertEntry(it.toEntity(matchId)) }
            dao.touch(matchId, clock())
        }
    }

    suspend fun deleteEntry(matchId: Long, cell: CellKey) {
        db.withTransaction {
            dao.deleteEntry(matchId, cell.playerId, cell.round)
            dao.touch(matchId, clock())
        }
    }

    /** Devolve células ao estado anterior; `null` significa que a célula estava vazia. */
    suspend fun restore(matchId: Long, cells: List<Pair<CellKey, Entry?>>) {
        db.withTransaction {
            cells.forEach { (cell, entry) ->
                if (entry == null) {
                    dao.deleteEntry(matchId, cell.playerId, cell.round)
                } else {
                    dao.upsertEntry(entry.toEntity(matchId))
                }
            }
            dao.touch(matchId, clock())
        }
    }

    suspend fun setFinished(matchId: Long, finished: Boolean) {
        val now = clock()
        dao.setFinished(matchId, if (finished) now else null, now)
    }

    suspend fun deleteMatch(matchId: Long) {
        db.withTransaction {
            dao.deleteEntries(matchId)
            dao.deleteMatchPlayers(matchId)
            dao.deleteMatch(matchId)
        }
    }

    companion object {
        fun from(context: Context): MatchRepository = MatchRepository(AppDatabase.get(context))
    }
}

private fun MatchEntity.toSnapshot(seats: List<SeatedPlayer>, entries: List<ScoreEntryEntity>) =
    MatchSnapshot(
        id = id,
        template = Templates.byId(templateId),
        win = if (lowestWins) WinCondition.LOWEST else WinCondition.HIGHEST,
        target = target,
        createdAt = createdAt,
        updatedAt = updatedAt,
        finishedAt = finishedAt,
        players = seats.sortedBy { it.seat }.map { MatchPlayer(it.playerId, it.name, it.seat) },
        entries = entries.map { Entry(it.playerId, it.round, it.value, EntryDetailCodec.decode(it.detailJson)) },
    )

private fun Entry.toEntity(matchId: Long) = ScoreEntryEntity(
    matchId = matchId,
    playerId = playerId,
    round = round,
    value = value,
    detailJson = EntryDetailCodec.encode(detail),
)
