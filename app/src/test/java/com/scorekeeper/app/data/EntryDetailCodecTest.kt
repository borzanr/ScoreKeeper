package com.scorekeeper.app.data

import com.scorekeeper.app.rules.EntryDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EntryDetailCodecTest {

    @Test
    fun `empty detail is stored as null`() {
        assertNull(EntryDetailCodec.encode(EntryDetail()))
    }

    @Test
    fun `detail survives a round trip`() {
        val detail = EntryDetail(cards = listOf(-2, 12), modifiers = listOf(4), times2 = true, closer = true, dice = listOf(1, 6))
        assertEquals(detail, EntryDetailCodec.decode(EntryDetailCodec.encode(detail)))
    }

    @Test
    fun `missing or corrupted text becomes an empty detail`() {
        assertEquals(EntryDetail(), EntryDetailCodec.decode(null))
        assertEquals(EntryDetail(), EntryDetailCodec.decode("{not json"))
    }

    @Test
    fun `unknown fields from a newer version are ignored`() {
        assertEquals(EntryDetail(bust = true), EntryDetailCodec.decode("""{"bust":true,"future":1}"""))
    }
}
