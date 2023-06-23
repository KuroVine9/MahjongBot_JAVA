package kuro9.mahjongbot.db.data

import kuro9.mahjongbot.annotation.IntRange
import kuro9.mahjongbot.annotation.UserRes


data class GameResult(
    val gameID: ULong,
    @UserRes val userID: ULong,
    @IntRange(1, 4) val rank: Int,
    val score: Int
)
