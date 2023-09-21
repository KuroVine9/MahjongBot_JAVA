package kuro9.mahjongbot.instruction

import kuro9.mahjongbot.Logger
import kuro9.mahjongbot.ResourceHandler
import kuro9.mahjongbot.db.DBHandler
import kuro9.mahjongbot.exception.DBConnectException
import kuro9.mahjongbot.instruction.util.GameDataParse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color

object GetAdminList : GameDataParse() {
    fun action(event: SlashCommandInteractionEvent) {
        val guildId: Long = getGuildID(event)
        val resourceBundle = ResourceHandler.getResource(event)
        event.deferReply().queue()

        try {
            val adminIdList = DBHandler.selectAdmin(guildId)
            val adminNameList = adminIdList.map { event.jda.getUserById(it)?.effectiveName ?: "<Unknown>" }

            event.hook.sendMessageEmbeds(
                EmbedBuilder().apply {
                    setTitle("200 OK")
                    addField(
                        resourceBundle.getString("admin.embed.get.title"),
                        adminNameList.joinToString(prefix = "[ ", postfix = " ]", transform = { "`$it`" }),
                        true
                    )
                    setColor(Color.BLACK)
                }.build()
            ).queue()

            Logger.addEvent(event)
        }
        catch (e: DBConnectException) {
            event.hook.sendMessageEmbeds(
                e.getErrorEmbed(event.userLocale)
            ).setEphemeral(true).queue()
        }
    }
}