package kuro9.mahjongbot.db.data

import com.google.gson.Gson
import kuro9.mahjongbot.annotation.GuildRes
import kuro9.mahjongbot.annotation.UserRes
import java.sql.Timestamp

data class Game(
    @GuildRes val guildID: Long,
    @UserRes val addedBy: Long,
    val gameGroup: String = ""
) {
    var id: Int = 0
    var createdAt: Timestamp = Timestamp(System.currentTimeMillis())

    override fun toString(): String {
        return Gson().toJson(this)
    }
}
