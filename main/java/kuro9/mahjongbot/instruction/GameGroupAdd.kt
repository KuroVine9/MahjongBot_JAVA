package kuro9.mahjongbot.instruction

import kuro9.mahjongbot.Logger
import kuro9.mahjongbot.ResourceHandler
import kuro9.mahjongbot.db.DBHandler
import kuro9.mahjongbot.exception.DBConnectException
import kuro9.mahjongbot.exception.ParameterErrorException
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color
import java.util.*

fun gameGroupAdd(event: SlashCommandInteractionEvent, gameGroup: String) {
    val guildId: Long = event.guild?.idLong ?: 0
    val resourceBundle = ResourceHandler.getResource(event)

    if (guildId == 0L) {
        val embed = EmbedBuilder()
        embed.setTitle("403 Forbidden")
        embed.addField(
            resourceBundle.getString("exception.not_in_guild.name"),
            resourceBundle.getString("exception.not_in_guild.description"),
            true
        )
        embed.setColor(Color.RED)
        event.hook.sendMessageEmbeds(embed.build()).queue()

        Logger.addErrorEvent(event, Logger.NOT_GUILD_MSG)
        return
    }
    if (!DBHandler.checkGameGroup(gameGroup)) {
        val embed = EmbedBuilder()
        embed.setTitle("400 Bad Requests")
        embed.addField(
            resourceBundle.getString("exception.not_in_guild.name"),
            resourceBundle.getString("exception.not_in_guild.description"),
            true
        )
        embed.setColor(Color.RED)
        event.hook.sendMessageEmbeds(embed.build()).queue()

        Logger.addErrorEvent(event, Logger.NOT_GUILD_MSG)
        return
    }

    try {
        DBHandler.addGameGroup(guildId, gameGroup)

        //TODO 성공 임베드, 현재 게임 그룹 가져오는 임베드

        val embed = EmbedBuilder()
        embed.setTitle(resourceBundle.getString("add.embed.success.title"))
        for (i in 0..3) {
            embed.addField(
                String.format(resourceBundle.getString("add.embed.success.field"), i + 1, names.get(i)),
                scores.get(i).toString(),
                true
            )
        }
        embed.setFooter(
            String.format(
                resourceBundle.getString("add.embed.success.footer"),
                game_count,
                game.createdAt
            )
        )
        embed.setColor(Color.BLACK)
        event.hook.sendMessageEmbeds(embed.build()).queue()
        Logger.addEvent(event)
    }
    catch (e: ParameterErrorException) {
        event.hook
            .sendMessageEmbeds(e.getErrorEmbed(event.userLocale).build())
            .setEphemeral(true)
            .queue()
        Logger.addErrorEvent(event, Logger.PARAM_ERR)
        return
    }
    catch (e: DBConnectException) {
        event.hook
            .sendMessageEmbeds(e.getErrorEmbed(event.userLocale).build())
            .setEphemeral(true)
            .queue()
        return
    }


}