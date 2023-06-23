package kuro9.mahjongbot.db.data

import kuro9.mahjongbot.annotation.GuildRes
import kuro9.mahjongbot.annotation.UserRes
import java.sql.Timestamp

data class Game(
    val id: ULong,
    @GuildRes val guildID: ULong,
    @UserRes val addedBy: ULong,
    val gameGroup: String,
    val createdAt: Timestamp
)
