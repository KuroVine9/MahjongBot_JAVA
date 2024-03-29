package kuro9.mahjongbot.instruction

import kuro9.mahjongbot.Logger
import kuro9.mahjongbot.ResourceHandler
import kuro9.mahjongbot.db.DBHandler
import kuro9.mahjongbot.exception.AddParameterErrorException
import kuro9.mahjongbot.exception.DBConnectException
import kuro9.mahjongbot.exception.getInvalidGameGroupErrorEmbed
import kuro9.mahjongbot.exception.getNotInGuildEmbed
import kuro9.mahjongbot.instruction.util.GameDataParse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color

object AddGameGroup : GameDataParse() {
    fun action(event: SlashCommandInteractionEvent) {
        val guildId: Long = getGuildID(event)
        val gameGroup: String = getGameGroup(event)
        val resourceBundle = ResourceHandler.getResource(event)

        event.deferReply().queue()

        if (guildId == 0L) {
            event.hook.sendMessageEmbeds(getNotInGuildEmbed(event.userLocale)).queue()

            Logger.addErrorEvent(event, Logger.NOT_GUILD_MSG)
            return
        }
        if (!DBHandler.checkGameGroup(gameGroup) || gameGroup == "") {
            event.hook.sendMessageEmbeds(getInvalidGameGroupErrorEmbed(event.userLocale)).queue()

            Logger.addErrorEvent(event, Logger.PARAM_ERR)
            return
        }


        try {
            val embed = EmbedBuilder()
            DBHandler.addGameGroup(guildId, gameGroup)
            val gameGroupList = DBHandler.selectGameGroup(guildId)

            embed.setTitle("200 OK")
            embed.addField(
                resourceBundle.getString("game_group.add.embed.success.field_title"),
                gameGroupList.joinToString(separator = "\n", transform = { "`$it`" }),
                false
            )
            embed.setFooter(
                String.format(
                    resourceBundle.getString("game_group.add.embed.success.footer"),
                    gameGroup
                )
            )
            embed.setColor(Color.BLACK)

            event.hook.sendMessageEmbeds(embed.build()).queue()
            Logger.addEvent(event)
        }
        catch (e: AddParameterErrorException) {
            event.hook.sendMessageEmbeds(getInvalidGameGroupErrorEmbed(event.userLocale)).queue()

            Logger.addErrorEvent(event, Logger.NOT_GUILD_MSG)
            return
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

