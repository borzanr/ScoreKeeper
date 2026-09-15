package com.scorekeeper.app.data

import com.scorekeeper.app.rules.EntryDetail
import kotlinx.serialization.json.Json

/** Converte [EntryDetail] para a coluna de texto do banco e de volta. */
object EntryDetailCodec {

    private val json = Json { ignoreUnknownKeys = true }

    /** Detalhe vazio (lançamento só com número) não ocupa espaço: vira `null`. */
    fun encode(detail: EntryDetail): String? =
        if (detail == EntryDetail()) null else json.encodeToString(EntryDetail.serializer(), detail)

    /** Texto ausente ou corrompido vira detalhe vazio; o valor da célula não se perde. */
    fun decode(raw: String?): EntryDetail = raw
        ?.let { runCatching { json.decodeFromString(EntryDetail.serializer(), it) }.getOrNull() }
        ?: EntryDetail()
}
