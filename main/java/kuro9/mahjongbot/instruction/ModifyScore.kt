package kuro9.mahjongbot.instruction

import kuro9.mahjongbot.Logger
import kuro9.mahjongbot.ResourceHandler
import kuro9.mahjongbot.db.DBHandler
import kuro9.mahjongbot.db.data.Game
import kuro9.mahjongbot.db.data.GameResult
import kuro9.mahjongbot.exception.EmbeddableException
import kuro9.mahjongbot.exception.ParameterErrorException
import kuro9.mahjongbot.exception.PermissionDeniedException
import kuro9.mahjongbot.exception.PermissionExpiredException
import kuro9.mahjongbot.instruction.util.GameDataParse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color
import java.util.*
import java.util.stream.IntStream

object ModifyScore : GameDataParse() {
    fun action(event: SlashCommandInteractionEvent) {
        val userId: Long = event.user.idLong
        val guildId: Long = getGuildID(event)
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

        if (guildId == 0L) {
            invalidGuildTask(event, resourceBundle)
            return
        }

        val guild = event.jda.getGuildById(guildId)

        if (guild === null) {
            invalidGuildTask(event, resourceBundle)
            return
        }

        if (event.user.idLong != guild.ownerIdLong) {
            event.hook.sendMessageEmbeds(
                EmbedBuilder().apply {
                    setTitle("403 Forbidden")
                    addField(
                        resourceBundle.getString("exception.no_permission.title"),
                        resourceBundle.getString("exception.no_permission.description"),
                        true
                    )
                    setColor(Color.RED)
                }.build()
            ).setEphemeral(true).queue()

            Logger.addErrorEvent(event, Logger.PERMISSION_DENY)
            return
        }

        val game = Game(guildId, userId).apply { id = gameId }
        val result =
            IntStream.range(0, 4).mapToObj { GameResult(gameId, resultUserIds[it]!!, it + 1, resultScores[it]!!) }
                .toList()

        try {
            DBHandler.modifyRecord(userId, game, result)

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