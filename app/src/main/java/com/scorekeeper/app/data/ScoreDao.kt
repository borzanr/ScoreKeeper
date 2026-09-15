package com.scorekeeper.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {

    // Partidas em andamento primeiro, depois as encerradas; cada grupo da mais recente.
    @Query("SELECT * FROM matches ORDER BY (finishedAt IS NOT NULL), updatedAt DESC")
    fun observeMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :matchId")
    fun observeMatch(matchId: Long): Flow<MatchEntity?>

    @Query(
        "SELECT mp.matchId AS matchId, mp.playerId AS playerId, mp.seat AS seat, p.name AS name " +
            "FROM match_players mp JOIN players p ON p.id = mp.playerId ORDER BY mp.matchId, mp.seat"
    )
    fun observeAllSeats(): Flow<List<SeatedPlayer>>

    @Query(
        "SELECT mp.matchId AS matchId, mp.playerId AS playerId, mp.seat AS seat, p.name AS name " +
            "FROM match_players mp JOIN players p ON p.id = mp.playerId " +
            "WHERE mp.matchId = :matchId ORDER BY mp.seat"
    )
    fun observeSeats(matchId: Long): Flow<List<SeatedPlayer>>

    @Query("SELECT * FROM score_entries")
    fun observeAllEntries(): Flow<List<ScoreEntryEntity>>

    @Query("SELECT * FROM score_entries WHERE matchId = :matchId")
    fun observeEntries(matchId: Long): Flow<List<ScoreEntryEntity>>

    @Query("SELECT * FROM players ORDER BY lastUsedAt DESC")
    fun observePlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE name = :name LIMIT 1")
    suspend fun findPlayer(name: String): PlayerEntity?

    @Insert
    suspend fun insertPlayer(player: PlayerEntity): Long

    @Update
    suspend fun updatePlayer(player: PlayerEntity)

    @Insert
    suspend fun insertMatch(match: MatchEntity): Long

    @Insert
    suspend fun insertMatchPlayers(players: List<MatchPlayerEntity>)

    @Upsert
    suspend fun upsertEntry(entry: ScoreEntryEntity)

    @Query("DELETE FROM score_entries WHERE matchId = :matchId AND playerId = :playerId AND round = :round")
    suspend fun deleteEntry(matchId: Long, playerId: Long, round: Int)

    @Query("UPDATE matches SET updatedAt = :now WHERE id = :matchId")
    suspend fun touch(matchId: Long, now: Long)

    @Query("UPDATE matches SET finishedAt = :finishedAt, updatedAt = :now WHERE id = :matchId")
    suspend fun setFinished(matchId: Long, finishedAt: Long?, now: Long)

    @Query("DELETE FROM score_entries WHERE matchId = :matchId")
    suspend fun deleteEntries(matchId: Long)

    @Query("DELETE FROM match_players WHERE matchId = :matchId")
    suspend fun deleteMatchPlayers(matchId: Long)

    @Query("DELETE FROM matches WHERE id = :matchId")
    suspend fun deleteMatch(matchId: Long)
}
