package com.scorekeeper.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta tirada da arte do mago: fundo quase-preto puxado para o roxo, dourado
 * das letras do brasão e o lilás do manto.
 */
val Ink = Color(0xFF0E0A14)
val Surface1 = Color(0xFF17111F)
val Surface2 = Color(0xFF221A2D)
val Outline = Color(0xFF3A2F48)

/** Dourado do brasão: reservado à ação principal. */
val MagicGold = Color(0xFFF2C14E)

/** Lilás do manto: títulos, ícones e destaques secundários. */
val RobeLilac = Color(0xFFB794F6)

/** Azul do cristal do cajado. */
val CrystalBlue = Color(0xFF7DD3FC)

val TextPrimary = Color(0xFFEFE9F5)
val TextSecondary = Color(0xFFA79BB5)
val Danger = Color(0xFFFF6B81)

/** Uma cor por lugar à mesa. Vizinhas bem diferentes entre si. */
private val PlayerColors = listOf(
    Color(0xFF7DD3FC),
    Color(0xFFF2C14E),
    Color(0xFFB4FF39),
    Color(0xFFFF6B81),
    Color(0xFFB794F6),
    Color(0xFF4ADE80),
    Color(0xFFFF8A3D),
    Color(0xFFF472B6),
)

fun playerColor(seat: Int): Color = PlayerColors[seat.mod(PlayerColors.size)]

/** Cores dos confetes (RGB int, formato exigido pelo Konfetti). */
val ConfettiColors = listOf(0xF2C14E, 0xB794F6, 0x7DD3FC, 0xFFE9A8, 0xFF6B81)
