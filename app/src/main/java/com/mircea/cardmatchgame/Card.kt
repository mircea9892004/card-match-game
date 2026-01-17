package com.mircea.cardmatchgame

data class Card(
    val id: Int,
    val emoji: String,
    var isFaceUp: Boolean = false,
    var isMatched: Boolean = false
)
