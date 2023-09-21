package kuro9.mahjongbot.instruction

import kuro9.mahjongbot.Logger
import kuro9.mahjongbot.ResourceHandler
import kuro9.mahjongbot.db.DBHandler
import kuro9.mahjongbot.exception.DBConnectException
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
            val embed = EmbedBuilder()
            embed.setTitle("403 Forbidden")
            embed.addField(
                resourceBundle.getString("exception.not_in_guild.title"),
                resourceBundle.getString("exception.not_in_guild.description"),
                true
            )
            embed.setColor(Color.RED)
            event.hook.sendMessageEmbeds(embed.build()).queue()

            Logger.addErrorEvent(event, Logger.NOT_GUILD_MSG)
            return
        }

        val guildName = event.jda.getGuildById(guildId)?.name ?: "<Fail-To-Load-Name>"

        try {
            val gameGroupList = DBHandler.selectGameGroup(guildId)

            event.hook.sendMessageEmbeds(
                EmbedBuilder().apply {
                    setTitle(String.format(resourceBundle.getString("gamegroup.select.embed.title"), guildName))
                    addField(
                        String.format(
                            resourceBundle.getString("gamegroup.select.embed.field_title"),
                            gameGroupList.size
                        ),
                        gameGroupList.joinToString(separator = "\n", transform = { "`$it`" }),
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