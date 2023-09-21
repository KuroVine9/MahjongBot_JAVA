package kuro9.mahjongbot.exception

import kuro9.mahjongbot.Logger
import kuro9.mahjongbot.ResourceHandler
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.awt.Color

abstract class EmbeddableException(message: String) : Exception(message) {
    abstract fun getErrorEmbed(locale: DiscordLocale): MessageEmbed
}

class DBConnectException(message: String = "DB Connect Error!") : EmbeddableException(message) {
    init {
        Logger.addSystemErrorEvent(Logger.DB_CONN_ERR)
    }

    override fun getErrorEmbed(locale: DiscordLocale): MessageEmbed {
        val resourceBundle = ResourceHandler.getResource(locale)
        val embed = EmbedBuilder()
        embed.setTitle("500 Internal Server Error")
        embed.addField(
            resourceBundle.getString("exception.dbconn.name"),
            resourceBundle.getString("exception.dbconn.description"),
            true
        )
        embed.setColor(Color.RED)
        return embed.build()
    }
}

class ParameterErrorException(message: String = "Param Error!") : EmbeddableException(message) {
    override fun getErrorEmbed(locale: DiscordLocale): MessageEmbed {
        val resourceBundle = ResourceHandler.getResource(locale)
        val embed = EmbedBuilder()
        embed.setTitle("400 Bad Request")
        embed.setDescription("Parameter err.")
        embed.addField(
            resourceBundle.getString("add.embed.err.400.name"),
            resourceBundle.getString("add.embed.err.400.description"),
            true
        )
        embed.setColor(Color.RED)
        return embed.build()
    }

}

class GameGroupNotFoundException(message: String = "Game Group Not Found!") : EmbeddableException(message) {
    override fun getErrorEmbed(locale: DiscordLocale): MessageEmbed {
        val resourceBundle = ResourceHandler.getResource(locale)
        val embed = EmbedBuilder()
        embed.setTitle("404 Not Found")
        embed.setDescription("Game Group Not Found")
        embed.addField(
            resourceBundle.getString("add.embed.err.404.name"),
            resourceBundle.getString("add.embed.err.404.description"),
            true
        )
        embed.setColor(Color.RED)
        return embed.build()
    }

}

class NotGuildInteractionException(message: String = "Not a Guild Interaction!") : EmbeddableException(message) {
    override fun getErrorEmbed(locale: DiscordLocale): MessageEmbed {
        val resourceBundle = ResourceHandler.getResource(locale)
        val embed = EmbedBuilder()
        embed.setTitle("403 Forbidden")
        embed.addField(
            resourceBundle.getString("exception.not_in_guild.name"),
            resourceBundle.getString("exception.not_in_guild.description"),
            true
        )
        embed.setColor(Color.RED)
        return embed.build()
    }

}

class PermissionDeniedException(message: String = "You have No Permission!") : EmbeddableException(message) {
    override fun getErrorEmbed(locale: DiscordLocale): MessageEmbed {
        val resourceBundle = ResourceHandler.getResource(locale)
        val embed = EmbedBuilder()
        embed.setTitle("403 Forbidden")
        embed.addField(
            resourceBundle.getString("exception.no_permission.name"),
            resourceBundle.getString("exception.no_permission.description"),
            true
        )
        embed.setColor(Color.RED)
        return embed.build()
    }
}

class PermissionExpiredException(message: String = "Too late!") : EmbeddableException(message) {
    override fun getErrorEmbed(locale: DiscordLocale): MessageEmbed {
        val resourceBundle = ResourceHandler.getResource(locale)
        val embed = EmbedBuilder()
        embed.setTitle("400 Bad Request")
        embed.addField(
            resourceBundle.getString("exception.no_permission.name"),
            resourceBundle.getString("exception.no_permission.description"),
            true
        )
        embed.setColor(Color.RED)
        return embed.build()
    }
}

