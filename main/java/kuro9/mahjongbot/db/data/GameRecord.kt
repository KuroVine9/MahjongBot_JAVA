package kuro9.mahjongbot.db.data

import com.google.gson.Gson

data class GameRecord(
    val game: Game,
    val gameResults: Array<GameResult>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameRecord

        return game.id == other.game.id
    }

    override fun hashCode(): Int {
        var result = game.hashCode()
        result = 31 * result + gameResults.contentHashCode()
        return result
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }
}
