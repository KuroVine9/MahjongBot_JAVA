package kuro9.mahjongbot.exception

import kuro9.mahjongbot.ResourceHandler
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.awt.Color

private const val CODE_200 = "200 OK"
private const val CODE_400 = "400 Bad Request"
private const val CODE_403 = "403 Forbidden"
private const val CODE_404 = "404 Not Found"
private const val CODE_500 = "500 Internal Server Error"
private const val CODE_503 = "503 Service Unavailable"

fun getParameterErrorEmbed(locale: DiscordLocale): MessageEmbed {
    val resourceBundle = ResourceHandler.getResource(locale)

    return EmbedBuilder().apply {
        setTitle(CODE_400)
        setDescription("Parameter err.")
        addField(
            resourceBundle.getString("add.embed.err.400.name"),
            resourceBundle.getString("add.embed.err.400.description"), //TODO 좀 더 일반적인 것으로 바꾸기
            true
        )
        setColor(Color.RED)
    }.build()
}

fun getInvalidGuildErrorEmbed(locale: DiscordLocale): MessageEmbed {
    val resourceBundle = ResourceHandler.getResource(locale)

    return EmbedBuilder().apply {
        setTitle(CODE_400)
        addField(
            resourceBundle.getString("exception.invalid_guild.title"),
            resourceBundle.getString("exception.invalid_guild.description"),
            true
        )
        setColor(Color.RED)
    }.build()
}

fun getInvalidGameGroupErrorEmbed(locale: DiscordLocale): MessageEmbed {
    val resourceBundle = ResourceHandler.getResource(locale)

    return EmbedBuilder().apply {
        setTitle("400 Bad Request")
        addField(
            resourceBundle.getString("game_group.add.embed.err.400.name"),
            resourceBundle.getString("game_group.add.embed.err.400.description"),
            true
        )
        setColor(Color.RED)
    }.build()
}

fun getNotInGuildEmbed(locale: DiscordLocale): MessageEmbed {
    val resourceBundle = ResourceHandler.getResource(locale)

    return EmbedBuilder().apply {
        setTitle(CODE_403)
        addField(
            resourceBundle.getString("exception.not_in_guild.title"),
            resourceBundle.getString("exception.not_in_guild.description"),
            true
        )
        setColor(Color.RED)
    }.build()
}

fun getNoPermissionEmbed(locale: DiscordLocale): MessageEmbed {
    val resourceBundle = ResourceHandler.getResource(locale)

    return EmbedBuilder().apply {
        setTitle(CODE_403)
        addField(
            resourceBundle.getString("exception.no_permission.title"),
            resourceBundle.getString("exception.no_permission.description"),
            true
        )
        setColor(Color.RED)
    }.build()
}

fun getPermissionExpiredEmbed(locale: DiscordLocale): MessageEmbed {
    val resourceBundle = ResourceHandler.getResource(locale)

    return EmbedBuilder().apply {
        setTitle(CODE_403)
        addField(
            resourceBundle.getString("exception.time_expired.title"),
            resourceBundle.getString("exception.time_expired.description"),
            true
        )
        setColor(Color.RED)

    }.build()
}

fun getGameGroupNotFoundEmbed(locale: DiscordLocale): MessageEmbed {
    val resourceBundle = ResourceHandler.getResource(locale)

    return EmbedBuilder().apply {
        setTitle(CODE_404)
        setDescription("Game Group Not Found")
        addField(
            resourceBundle.getString("add.embed.err.404.name"),
            resourceBundle.getString("add.embed.err.404.description"),
            true
        )
        setColor(Color.RED)
    }.build()
}

fun getGameDataNotFoundEmbed(locale: DiscordLocale): MessageEmbed {
    val resourceBundle = ResourceHandler.getResource(locale)

    return EmbedBuilder().apply {
        setTitle(CODE_404)
        addField(
            resourceBundle.getString("exception.not_found.title"),
            resourceBundle.getString("exception.not_found.description"),
            true
        )
        setColor(Color.RED)
    }.build()
}

fun getParseErrorEmbed(locale: DiscordLocale): MessageEmbed {
    val resourceBundle = ResourceHandler.getResource(locale)

    return EmbedBuilder().apply {
        setTitle(CODE_500)
        addField(
            resourceBundle.getString("exception.parse_err.title"),
            resourceBundle.getString("exception.parse_err.description"),
            true
        )
        setColor(Color.RED)
    }.build()
}

fun getDBConnectErrorEmbed(locale: DiscordLocale): MessageEmbed {
    val resourceBundle = ResourceHandler.getResource(locale)

    return EmbedBuilder().apply {
        setTitle(CODE_503)
        addField(
            resourceBundle.getString("exception.dbconn.title"),
            resourceBundle.getString("exception.dbconn.description"),
            true
        )
        setColor(Color.RED)
    }.build()
}
