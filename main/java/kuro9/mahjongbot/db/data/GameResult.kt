package kuro9.mahjongbot.db.data

import com.google.gson.Gson
import kuro9.mahjongbot.annotation.IntRange
import kuro9.mahjongbot.annotation.UserRes


data class GameResult(
    val gameID: Int = 0,
    @UserRes val userID: Long,
    @IntRange(1, 4) val rank: Int,
    val score: Int,
    val name: String?
) {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}
