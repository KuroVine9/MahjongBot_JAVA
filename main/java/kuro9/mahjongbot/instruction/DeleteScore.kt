package kuro9.mahjongbot.instruction

import kuro9.mahjongbot.Logger
import kuro9.mahjongbot.ResourceHandler
import kuro9.mahjongbot.db.DBHandler
import kuro9.mahjongbot.exception.DBConnectException
import kuro9.mahjongbot.instruction.util.GameDataParse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color

object DeleteScore : GameDataParse() {
    fun action(event: SlashCommandInteractionEvent) {
        val userId: Long = event.user.idLong
        val guildId: Long = getGuildID(event)
        val gameId: Int? = event.getOption("game_id")?.asInt
        val resourceBundle = ResourceHandler.getResource(event)
        event.deferReply().queue()

        lateinit var adminList: List<Long>

        if (gameId === null) {

            event.hook.sendMessageEmbeds(
                EmbedBuilder().apply {
                    setTitle("500 Internal Server Error")
                    addField(
                        resourceBundle.getString("exception.parse_err.title"),
                        resourceBundle.getString("exception.parse_err.description"),
                        true
                    )
                }.build()
            ).setEphemeral(true).queue()

            Logger.addErrorEvent(event, Logger.PARAM_PARSE_ERR)
            return
        }

        try {
            adminList = DBHandler.selectAdmin(guildId)
        }
        catch (e: DBConnectException) {
            event.hook.sendMessageEmbeds(e.getErrorEmbed(event.userLocale)).setEphemeral(true).queue()
            return
        }

        if (!adminList.contains(userId)) {
            val embed = EmbedBuilder()
            embed.setTitle("403 Forbidden")
            embed.addField(
                resourceBundle.getString("exception.no_permission.title"),
                resourceBundle.getString("exception.no_permission.description"),
                true
            )
            embed.setColor(Color.RED)
            event.hook.sendMessageEmbeds(embed.build()).queue()

            Logger.addErrorEvent(event, Logger.PERMISSION_DENY)
            return
        }

        try {
            DBHandler.deleteRecord(userId, gameId, guildId)

            event.hook.sendMessageEmbeds(
                EmbedBuilder().apply {
                    setTitle("200 OK")
                    setDescription(String.format(resourceBundle.getString("delete_score.embed.description"), gameId))
                    setColor(Color.BLACK)
                }.build()
            ).queue()

            Logger.addEvent(event)

        }
        catch (e: DBConnectException) {
            event.hook.sendMessageEmbeds(e.getErrorEmbed(event.userLocale)).setEphemeral(true).queue()
            return
        }
    }
}