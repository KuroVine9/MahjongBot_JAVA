package kuro9.mahjongbot.instruction

import kuro9.mahjongbot.DBScoreProcess
import kuro9.mahjongbot.Logger
import kuro9.mahjongbot.ResourceHandler
import kuro9.mahjongbot.db.data.GameResult
import kuro9.mahjongbot.exception.*
import kuro9.mahjongbot.instruction.util.GameDataParse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color
import java.util.*

object ModifyScore : GameDataParse() {
    fun action(event: SlashCommandInteractionEvent) {
        val userId: Long = event.user.idLong
        val gameId: Int? = event.getOption("game_id")?.asInt

        val resultUserIds = listOf(
            event.getOption("1st_name")?.asUser?.idLong,
            event.getOption("2nd_name")?.asUser?.idLong,
            event.getOption("3rd_name")?.asUser?.idLong,
            event.getOption("4th_name")?.asUser?.idLong
        )

        val resultUserNames = listOf(
            event.getOption("1st_name")?.asUser?.effectiveName,
            event.getOption("2nd_name")?.asUser?.effectiveName,
            event.getOption("3rd_name")?.asUser?.effectiveName,
            event.getOption("4th_name")?.asUser?.effectiveName
        )

        val resultScores = listOf(
            event.getOption("1st_score")?.asInt,
            event.getOption("2nd_score")?.asInt,
            event.getOption("3rd_score")?.asInt,
            event.getOption("4th_score")?.asInt
        )

        val resourceBundle = ResourceHandler.getResource(event)

        if (gameId === null || resultUserIds.any { it === null } || resultScores.any { it === null }) {

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

        event.deferReply().queue()


        val result =
            (0..3).map { GameResult(gameId, resultUserIds[it]!!, it + 1, resultScores[it]!!) }
                .toList()

        try {
            DBScoreProcess.modifyRecord(userId, gameId, result)

            event.hook.sendMessageEmbeds(
                EmbedBuilder().apply {
                    setTitle(resourceBundle.getString("modify_score.embed.title"))
                    repeat(4) {
                        addField(
                            String.format(
                                resourceBundle.getString("modify_score.embed.field"),
                                it + 1,
                                resultUserNames[it]
                            ),
                            resultScores[it]!!.toString(),
                            true
                        )
                    }
                    setFooter(String.format(resourceBundle.getString("modify_score.embed.footer"), gameId))
                }.build()
            ).queue()

            Logger.addEvent(event)
        }
        catch (e: EmbeddableException) {
            event.hook.sendMessageEmbeds(e.getErrorEmbed(event.userLocale)).setEphemeral(true).queue()

            when (e) {
                is ParameterErrorException ->
                    Logger.addErrorEvent(event, Logger.PARAM_ERR)

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

    private fun invalidGuildTask(event: SlashCommandInteractionEvent, resourceBundle: ResourceBundle) {
        val embed = EmbedBuilder()
        embed.setTitle("400 Bad Request")
        embed.addField(
            resourceBundle.getString("exception.invalid_guild.title"),
            resourceBundle.getString("exception.invalid_guild.description"),
            true
        )
        embed.setColor(Color.RED)
        event.hook.sendMessageEmbeds(embed.build()).queue()

        Logger.addErrorEvent(event, Logger.UNKNOWN_GUILD)
        return
    }
}