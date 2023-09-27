package kuro9.mahjongbot.instruction

import kuro9.mahjongbot.DBScoreProcess
import kuro9.mahjongbot.Logger
import kuro9.mahjongbot.ResourceHandler
import kuro9.mahjongbot.db.DBHandler
import kuro9.mahjongbot.exception.EmbeddableException
import kuro9.mahjongbot.exception.GameNotFoundException
import kuro9.mahjongbot.exception.PermissionDeniedException
import kuro9.mahjongbot.exception.PermissionExpiredException
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
        val resourceBundle = ResourceHandler.getResource(event)
        event.deferReply().queue()

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

        val key = String(Base64.getEncoder().encode("userID=${userId}, gameID=${gameId}".toByteArray()))

        try {
            val gameRecord = DBHandler.getGameData(gameId)

            event.hook.sendMessageEmbeds(
                EmbedBuilder().apply {
                    setTitle("대충 삭제 확인 메시지?")//TODO
                    setFooter(key)

                    (0..3).forEach {
                        addField(
                            "${it + 1}위=${event.jda.retrieveUserById(gameRecord.gameResults[it].userID).complete()}",
                            gameRecord.gameResults[it].score.toString(),
                            true
                        )
                    }
                    setColor(Color.YELLOW)
                }.build()
            ).setActionRow(confirmButton, cancelButton).queue()

            Logger.addEvent(event)

        } catch (e: EmbeddableException) {
            event.hook.sendMessageEmbeds(e.getErrorEmbed(event.userLocale)).setEphemeral(true).queue()

            if (e is GameNotFoundException)
                Logger.addErrorEvent(event, Logger.GAME_NOT_FOUND)
        }
    }

    fun confirm(event: ButtonInteractionEvent) {
        var userId: Long? = null
        var gameId: Int? = null
        val guildId: Long? = event.guild?.idLong
        val resourceBundle = ResourceHandler.getResource(event)
        event.deferEdit().queue()


        val key = event.message.embeds[0].footer?.text?.let {
            "^key=(.+)".toRegex().find(it)?.groupValues?.get(1)
        }
        val decodedKey = String(Base64.getDecoder().decode(key))
        userId = decodedKey.let { "userID=(\\d+)".toRegex().find(it)?.groupValues?.get(1)?.toLong() }
        gameId = decodedKey.let { "gameID=(\\d+)".toRegex().find(it)?.groupValues?.get(1)?.toInt() }


        if (gameId === null || userId === null) {
            event.editOriginalWithNoButton(
                EmbedBuilder().apply {
                    setTitle("500 Internal Server Error")
                    addField(
                        resourceBundle.getString("exception.parse_err.title"),
                        resourceBundle.getString("exception.parse_err.description"),
                        true
                    )
                }.build()
            )

            Logger.addErrorEvent(event, Logger.PARAM_PARSE_ERR)
            return
        }

        if (guildId === null) {
            event.editOriginalWithNoButton(
                EmbedBuilder().apply {
                    setTitle("403 Forbidden")
                    addField(
                        resourceBundle.getString("exception.not_in_guild.title"),
                        resourceBundle.getString("exception.not_in_guild.description"),
                        true
                    )
                    setColor(Color.RED)
                }.build()
            )

            Logger.addErrorEvent(event, Logger.NOT_GUILD_MSG)
            return
        }

        if (userId != event.user.idLong) {
            event.hook.sendMessageEmbeds(
                EmbedBuilder().apply {
                    setTitle("403 Forbidden")
                    setDescription("메시지의 주체와 이벤트의 유저가 일치하지 않습니다. 만약 본인이 /delete를 사용하셨다면 잠시 뒤 다시 시도해 주세요.")
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
                    setDescription("Request cancelled")
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
                    setDescription(String.format(resourceBundle.getString("delete_score.embed.description"), gameId))
                    setColor(Color.BLACK)
                }.build()
            )

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

    private fun ButtonInteractionEvent.editOriginalWithNoButton(embed: MessageEmbed) =
        this.hook.editOriginalEmbeds(embed).setActionRow().queue()
}