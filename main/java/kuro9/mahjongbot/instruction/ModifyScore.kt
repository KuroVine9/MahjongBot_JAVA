package kuro9.mahjongbot.instruction

import kuro9.mahjongbot.Logger
import kuro9.mahjongbot.ResourceHandler
import kuro9.mahjongbot.db.DBHandler
import kuro9.mahjongbot.db.data.GameResult
import kuro9.mahjongbot.exception.*
import kuro9.mahjongbot.instruction.util.GameDataParse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.awt.Color
import java.sql.Timestamp
import java.util.*

object ModifyScore : GameDataParse() {
    private val confirmButton: Button = Button.danger("modify_confirm", "DELETE")
    private val cancelButton: Button = Button.secondary("modify_cancel", "CANCEL")
    fun action(event: SlashCommandInteractionEvent) {
        val userId: Long = event.user.idLong
        val gameId: Int? = event.getOption("game_id")?.asInt
        val guildId: Long? = event.guild?.idLong

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

        if (gameId === null || resultUserIds.any { it == null } || resultScores.any { it == null }) {

            event.hook.sendMessageEmbeds(getParseErrorEmbed(event.userLocale)).setEphemeral(true).queue()

            Logger.addErrorEvent(event, Logger.PARAM_PARSE_ERR)
            return
        }

        if (guildId === null) {
            event.hook.sendMessageEmbeds(getNotInGuildEmbed(event.userLocale)).setEphemeral(true).queue()
            Logger.addErrorEvent(event, Logger.NOT_GUILD_MSG)
            return
        }

        event.deferReply().queue()


        val result =
            (0..3).map { GameResult(gameId, resultUserIds[it]!!, it + 1, resultScores[it]!!) }
                .toList()

        val key = String(Base64.getEncoder().encode("userID=${userId}, gameID=${gameId}".toByteArray()))

        try {
            if (!DBHandler.selectAdmin(guildId).contains(userId))
                throw PermissionDeniedException()

            val gameRecord = DBHandler.getGameData(gameId)
            DBHandler.addTempScore(userId, guildId, gameId, result)

            event.hook.sendMessageEmbeds(
                EmbedBuilder().apply {
                    setTitle(resourceBundle.getString("modify.embed.alert.title"))
                    setDescription("GameID=$gameId")
                    setFooter("key=$key")

                    (0..3).forEach {
                        addField(
                            String.format(
                                resourceBundle.getString("modify.embed.alert.field.title"),
                                it + 1,
                                event.jda.retrieveUserById(gameRecord.gameResults[it].userID).complete().effectiveName,
                                resultUserNames[it]
                            ),
                            String.format(
                                "%d -> %d",
                                gameRecord.gameResults[it].score,
                                resultScores[it]
                            ),
                            false
                        )
                    }
                    setColor(Color.YELLOW)
                }.build()
            ).setActionRow(confirmButton, cancelButton).queue()

            Logger.addEvent(event)
        }
        catch (e: EmbeddableException) {
            event.hook.sendMessageEmbeds(e.getErrorEmbed(event.userLocale)).setEphemeral(true).queue()

            when (e) {
                is AddParameterErrorException ->
                    Logger.addErrorEvent(event, Logger.PARAM_ERR)

                is PermissionExpiredException ->
                    Logger.addErrorEvent(event, Logger.TIMEOUT)

                is PermissionDeniedException ->
                    Logger.addErrorEvent(event, Logger.PERMISSION_DENY)

                is GameNotFoundException ->
                    Logger.addErrorEvent(event, Logger.GAME_NOT_FOUND)

                is DataConflictException ->
                    Logger.addErrorEvent(event, Logger.DATA_CONFLICT)

                is DBConnectException -> {}

                else -> throw IllegalStateException()
            }
        }
    }

    fun confirm(event: ButtonInteractionEvent) {
        var userId: Long? = null
        var gameId: Int? = null
        val guildId: Long? = event.guild?.idLong
        val resourceBundle = ResourceHandler.getResource(event)
        event.deferEdit().queue()


        val key = event.message.embeds[0].footer?.text?.let {
            "key=([^=]+)==".toRegex().find(it)?.groupValues?.get(1)
        }
        val decodedKey = String(Base64.getDecoder().decode(key))
        userId = decodedKey.let { "userID=(\\d+)".toRegex().find(it)?.groupValues?.get(1)?.toLong() }
        gameId = decodedKey.let { "gameID=(\\d+)".toRegex().find(it)?.groupValues?.get(1)?.toInt() }


        if (gameId === null || userId === null) {
            event.editOriginalWithNoButton(getParseErrorEmbed(event.userLocale))

            Logger.addErrorEvent(event, Logger.PARAM_PARSE_ERR)
            return
        }

        if (guildId === null) {
            event.editOriginalWithNoButton(getNotInGuildEmbed(event.userLocale))

            Logger.addErrorEvent(event, Logger.NOT_GUILD_MSG)
            return
        }

        if (userId != event.user.idLong) {
            event.hook.sendMessageEmbeds(

                EmbedBuilder().apply {
                    setTitle("403 Forbidden")
                    setDescription(resourceBundle.getString("modify.embed.err.403.description"))
                    setColor(Color.RED)
                }.build()
            ).setEphemeral(true).queue()

            Logger.addErrorEvent(event, Logger.INVALID_USER)
            return
        }

        if (event.interaction.componentId == cancelButton.id) {

            try {
                DBHandler.getTempScore(userId, guildId, gameId)

                event.editOriginalWithNoButton(
                    EmbedBuilder().apply {
                        setTitle("200 OK")
                        setDescription(resourceBundle.getString("delete.embed.cancel.description"))
                        setColor(Color.BLACK)
                    }.build()
                )

                Logger.addEvent(event)
                return
            }
            catch (e: DBConnectException) {
                event.editOriginalWithNoButton(e.getErrorEmbed(event.userLocale))
            }
            catch (e: GameNotFoundException) {
                event.editOriginalWithNoButton(e.getErrorEmbed(event.userLocale))
                Logger.addErrorEvent(event, Logger.GAME_NOT_FOUND)
            }
        }

        try {
            val result = DBHandler.getTempScore(userId, guildId, gameId)
            DBHandler.modifyRecord(userId, gameId, guildId, result)

            event.editOriginalWithNoButton(
                EmbedBuilder(event.message.embeds[0]).apply {
                    setTitle("200 OK")
                    setDescription(
                        String.format(
                            resourceBundle.getString("modify.embed.success.description"),
                            gameId
                        )
                    )
                    setFooter(Timestamp(System.currentTimeMillis()).toString())
                    setColor(Color.WHITE)
                }.build()
            )
        }
        catch (e: EmbeddableException) {
            event.editOriginalWithNoButton(e.getErrorEmbed(event.userLocale))

            when (e) {
                is AddParameterErrorException ->
                    Logger.addErrorEvent(event, Logger.PARAM_ERR)

                is PermissionExpiredException ->
                    Logger.addErrorEvent(event, Logger.TIMEOUT)

                is PermissionDeniedException ->
                    Logger.addErrorEvent(event, Logger.PERMISSION_DENY)

                is GameNotFoundException ->
                    Logger.addErrorEvent(event, Logger.GAME_NOT_FOUND)

                is DBConnectException -> {}

                else -> throw IllegalStateException()
            }
        }

    }

    private fun invalidGuildTask(event: SlashCommandInteractionEvent) {
        event.hook.sendMessageEmbeds(getInvalidGuildErrorEmbed(event.userLocale)).queue()

        Logger.addErrorEvent(event, Logger.UNKNOWN_GUILD)
        return
    }

    private fun ButtonInteractionEvent.editOriginalWithNoButton(embed: MessageEmbed) {
        this.hook.editOriginalEmbeds(embed).setComponents().queue()
    }
}