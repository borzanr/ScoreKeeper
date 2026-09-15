package com.scorekeeper.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Jogador salvo para reaparecer como sugestão nas próximas partidas. */
@Entity(tableName = "players", indices = [Index(value = ["name"], unique = true)])
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    // NOCASE: "Ana" e "ana" são a mesma pessoa.
    @ColumnInfo(collate = ColumnInfo.NOCASE) val name: String,
    val lastUsedAt: Long,
)

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: String,
    val lowestWins: Boolean,
    /** Pontos ou número de rodadas, conforme o EndType do modelo. */
    val target: Int?,
    val createdAt: Long,
    val updatedAt: Long,
    val finishedAt: Long? = null,
)

@Entity(
    tableName = "match_players",
    primaryKeys = ["matchId", "playerId"],
    indices = [Index("playerId")],
)
data class MatchPlayerEntity(
    val matchId: Long,
    val playerId: Long,
    val seat: Int,
)

/** Uma célula do placar. A chave composta faz do upsert "lançar ou corrigir". */
@Entity(
    tableName = "score_entries",
    primaryKeys = ["matchId", "playerId", "round"],
)
data class ScoreEntryEntity(
    val matchId: Long,
    val playerId: Long,
    val round: Int,
    val value: Int,
    val detailJson: String?,
)

/** Linha de leitura: jogador de uma partida com o nome já resolvido. */
data class SeatedPlayer(
    val matchId: Long,
    val playerId: Long,
    val seat: Int,
    val name: String,
)
