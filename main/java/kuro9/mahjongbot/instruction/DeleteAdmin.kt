package kuro9.mahjongbot.instruction

import kuro9.mahjongbot.Logger
import kuro9.mahjongbot.ResourceHandler
import kuro9.mahjongbot.db.DBHandler
import kuro9.mahjongbot.exception.DBConnectException
import kuro9.mahjongbot.exception.getInvalidGuildErrorEmbed
import kuro9.mahjongbot.exception.getNoPermissionEmbed
import kuro9.mahjongbot.instruction.util.GameDataParse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color

object DeleteAdmin : GameDataParse() {
    fun action(event: SlashCommandInteractionEvent) {
        val userId: Long = getUserID(event)
        val guildId: Long = getGuildID(event)
        val resourceBundle = ResourceHandler.getResource(event)

        event.deferReply().queue()

        if (guildId == 0L) {
            invalidGuildTask(event)
            return
        }

        val guild = event.jda.getGuildById(guildId)

        if (guild === null) {
            invalidGuildTask(event)
            return
        }

        if (event.user.idLong != guild.ownerIdLong) {
            event.hook.sendMessageEmbeds(
                getNoPermissionEmbed(event.userLocale)
            ).setEphemeral(true).queue()

            Logger.addErrorEvent(event, Logger.PERMISSION_DENY)
            return
        }

        try {
            DBHandler.deleteAdmin(userId, guildId)

            event.hook.sendMessageEmbeds(
                EmbedBuilder().apply {
                    setTitle("200 OK")
                    addField(
                        resourceBundle.getString("admin.embed.delete.title"),
                        String.format(
                            resourceBundle.getString("admin.embed.delete.description"),
                            event.jda.retrieveUserById(userId).complete().effectiveName
                        ),
                        true
                    )
                    setColor(Color.BLACK)
                }.build()
            ).setEphemeral(true).queue()

            Logger.addEvent(event);
        }
        catch (e: DBConnectException) {
            event.hook.sendMessageEmbeds(
                e.getErrorEmbed(event.userLocale)
            ).setEphemeral(true).queue()
        }
    }

    private fun invalidGuildTask(event: SlashCommandInteractionEvent) {
        event.hook.sendMessageEmbeds(getInvalidGuildErrorEmbed(event.userLocale)).queue()

        Logger.addErrorEvent(event, Logger.UNKNOWN_GUILD)
        return
    }
}