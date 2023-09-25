package kuro9.mahjongbot.instruction

import kuro9.mahjongbot.DBScoreProcess
import kuro9.mahjongbot.Logger
import kuro9.mahjongbot.ResourceHandler
import kuro9.mahjongbot.exception.EmbeddableException
import kuro9.mahjongbot.exception.GameNotFoundException
import kuro9.mahjongbot.exception.PermissionDeniedException
import kuro9.mahjongbot.exception.PermissionExpiredException
import kuro9.mahjongbot.instruction.util.GameDataParse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color

object DeleteScore : GameDataParse() {
    fun action(event: SlashCommandInteractionEvent) {
        val userId: Long = event.user.idLong
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
            DBScoreProcess.deleteScore(userId, gameId)

            event.hook.sendMessageEmbeds(
                EmbedBuilder().apply {
                    setTitle("200 OK")
                    setDescription(String.format(resourceBundle.getString("delete_score.embed.description"), gameId))
                    setColor(Color.BLACK)
                }.build()
            ).queue()

            Logger.addEvent(event)

        } catch (e: EmbeddableException) {
            event.hook.sendMessageEmbeds(e.getErrorEmbed(event.userLocale)).setEphemeral(true).queue()

            when (e) {
                is PermissionExpiredException ->
                    Logger.addErrorEvent(event, Logger.TIMEOUT)

                is PermissionDeniedException ->
                    Logger.addErrorEvent(event, Logger.PERMISSION_DENY)

                is GameNotFoundException ->
                    Logger.addErrorEvent(event, Logger.GAME_NOT_FOUND)

                else -> {
                    // DO NOTHING
                }
            }
        }
    }
}