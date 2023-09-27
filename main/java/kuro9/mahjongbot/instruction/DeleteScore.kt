package kuro9.mahjongbot.instruction

import kuro9.mahjongbot.DBScoreProcess
import kuro9.mahjongbot.Logger
import kuro9.mahjongbot.ResourceHandler
import kuro9.mahjongbot.db.DBHandler
import kuro9.mahjongbot.exception.*
import kuro9.mahjongbot.instruction.util.GameDataParse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.awt.Color
import java.util.*

object DeleteScore : GameDataParse() {
    private val confirmButton: Button = Button.danger("delete_confirm", "DELETE")
    private val cancelButton: Button = Button.secondary("delete_cancel", "CANCEL")
    fun action(event: SlashCommandInteractionEvent) {
        val userId: Long = event.user.idLong
        val gameId: Int? = event.getOption("game_id")?.asInt
        val guildId: Long? = event.guild?.idLong
        val resourceBundle = ResourceHandler.getResource(event)
        event.deferReply().queue()

        if (gameId === null) {
            event.hook.sendMessageEmbeds(
                getParseErrorEmbed(event.userLocale)
            ).setEphemeral(true).queue()

            Logger.addErrorEvent(event, Logger.PARAM_PARSE_ERR)
            return
        }

        if (guildId === null) {
            event.hook.sendMessageEmbeds(
                getNotInGuildEmbed(event.userLocale)
            ).queue()
            return
        }

        val key = String(Base64.getEncoder().encode("userID=${userId}, gameID=${gameId}".toByteArray()))

        try {
            if (!DBHandler.selectAdmin(guildId).contains(userId))
                throw PermissionDeniedException()

            val gameRecord = DBHandler.getGameData(gameId)

            event.hook.sendMessageEmbeds(
                EmbedBuilder().apply {
                    setTitle(resourceBundle.getString("delete.embed.alert.title"))
                    setDescription("GameID=$gameId")
                    setFooter("key=$key")

                    (0..3).forEach {
                        addField(
                            String.format(
                                resourceBundle.getString("delete.embed.alert.field"),
                                it + 1,
                                event.jda.retrieveUserById(gameRecord.gameResults[it].userID).complete().effectiveName
                            ),
                            gameRecord.gameResults[it].score.toString(),
                            true
                        )
                    }
                    setColor(Color.YELLOW)
                }.build()
            ).setActionRow(confirmButton, cancelButton).queue()

            Logger.addEvent(event)

        }
        catch (e: EmbeddableException) {
            event.hook.sendMessageEmbeds(e.getErrorEmbed(event.userLocale)).setEphemeral(
                when (e) {
                    is DBConnectException -> true
                    is PermissionDeniedException -> {
                        Logger.addErrorEvent(event, Logger.PERMISSION_DENY)
                        false
                    }

                    is GameNotFoundException -> {
                        Logger.addErrorEvent(event, Logger.GAME_NOT_FOUND)
                        false
                    }

                    else -> {
                        throw IllegalStateException()
                    }
                }
            ).queue()
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
                    setDescription(resourceBundle.getString("delete.embed.err.403.description"))
                    setColor(Color.RED)
                }.build()
            ).setEphemeral(true).queue()

            Logger.addErrorEvent(event, Logger.INVALID_USER)
            return
        }

        if (event.interaction.componentId == cancelButton.id) {
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

        try {
            DBScoreProcess.deleteScore(userId, gameId, guildId)

            event.editOriginalWithNoButton(
                EmbedBuilder().apply {
                    setTitle("200 OK")
                    setDescription(String.format(resourceBundle.getString("delete.embed.success.description"), gameId))
                    setColor(Color.BLACK)
                }.build()
            )

            Logger.addEvent(event)
        }
        catch (e: EmbeddableException) {
            event.editOriginalWithNoButton(e.getErrorEmbed(event.userLocale))

            when (e) {
                is PermissionExpiredException ->
                    Logger.addErrorEvent(event, Logger.TIMEOUT)

                is PermissionDeniedException ->
                    Logger.addErrorEvent(event, Logger.PERMISSION_DENY)

                is GameNotFoundException ->
                    Logger.addErrorEvent(event, Logger.GAME_NOT_FOUND)

                else -> {
                    throw IllegalStateException()
                }
            }
        }
    }

    private fun ButtonInteractionEvent.editOriginalWithNoButton(embed: MessageEmbed) {
        this.hook.editOriginalEmbeds(embed).setComponents().queue()
    }
}