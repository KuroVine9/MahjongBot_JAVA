package kuro9.mahjongbot.instruction

import kuro9.mahjongbot.Logger
import kuro9.mahjongbot.ResourceHandler
import kuro9.mahjongbot.db.DBHandler
import kuro9.mahjongbot.exception.DBConnectException
import kuro9.mahjongbot.exception.getNotInGuildEmbed
import kuro9.mahjongbot.instruction.util.GameDataParse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color

object GetGameGroupList : GameDataParse() {
    fun action(event: SlashCommandInteractionEvent) {
        val guildId = getGuildID(event)
        val resourceBundle = ResourceHandler.getResource(event)

        event.deferReply().queue()

        if (guildId == 0L) {
            event.hook.sendMessageEmbeds(getNotInGuildEmbed(event.userLocale)).queue()

            Logger.addErrorEvent(event, Logger.NOT_GUILD_MSG)
            return
        }

        val guildName = event.jda.getGuildById(guildId)?.name ?: "<Fail-To-Load-Name>"

        try {
            val gameGroupList = DBHandler.selectGameGroup(guildId)

            event.hook.sendMessageEmbeds(
                EmbedBuilder().apply {
                    setTitle(String.format(resourceBundle.getString("game_group.select.embed.title"), guildName))
                    addField(
                        String.format(
                            resourceBundle.getString("game_group.select.embed.field_title"),
                            gameGroupList.size
                        ),
                        gameGroupList.joinToString(prefix = "[ ", postfix = " ]", transform = { "`$it`" }),
                        false
                    )
                    setColor(Color.BLACK)
                }.build()
            ).queue()

            Logger.addEvent(event)
        }
        catch (e: DBConnectException) {
            event.hook
                .sendMessageEmbeds(e.getErrorEmbed(event.userLocale))
                .setEphemeral(true)
                .queue()
            return
        }
    }
}