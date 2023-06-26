package kuro9.mahjongbot.db.data

import kuro9.mahjongbot.annotation.GuildRes
import kuro9.mahjongbot.annotation.UserRes

data class Admin(
    @UserRes val userID: Long,
    @GuildRes val guildID: Long
)
