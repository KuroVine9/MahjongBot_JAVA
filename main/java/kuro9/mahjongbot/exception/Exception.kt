package kuro9.mahjongbot.exception

import kuro9.mahjongbot.Logger
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.DiscordLocale

abstract class EmbeddableException(message: String) : Exception(message) {
    abstract fun getErrorEmbed(locale: DiscordLocale): MessageEmbed
}

class DBConnectException(message: String = "DB Connect Error!") : EmbeddableException(message) {
    init {
        Logger.addSystemErrorEvent(Logger.DB_CONN_ERR)
    }

    override fun getErrorEmbed(locale: DiscordLocale): MessageEmbed = getDBConnectErrorEmbed(locale)
}


class AddParameterErrorException(message: String = "Param Error!") : EmbeddableException(message) {
    override fun getErrorEmbed(locale: DiscordLocale): MessageEmbed = getAddParameterErrorEmbed(locale)

}

class InvalidGameGroupPatternException(message: String = "Not valid GameGroup!") : EmbeddableException(message) {
    override fun getErrorEmbed(locale: DiscordLocale): MessageEmbed = getInvalidGameGroupErrorEmbed(locale)
}

class GameGroupNotFoundException(message: String = "Game Group Not Found!") : EmbeddableException(message) {
    override fun getErrorEmbed(locale: DiscordLocale): MessageEmbed = getGameGroupNotFoundEmbed(locale)

}

class NotGuildInteractionException(message: String = "Not a Guild Interaction!") : EmbeddableException(message) {
    override fun getErrorEmbed(locale: DiscordLocale): MessageEmbed = getNotInGuildEmbed(locale)

}

class PermissionDeniedException(message: String = "You have No Permission!") : EmbeddableException(message) {
    override fun getErrorEmbed(locale: DiscordLocale): MessageEmbed = getNoPermissionEmbed(locale)
}

class PermissionExpiredException(message: String = "Too late!") : EmbeddableException(message) {
    override fun getErrorEmbed(locale: DiscordLocale): MessageEmbed = getPermissionExpiredEmbed(locale)
}

class GameNotFoundException(message: String = "GameData Not Found!") : EmbeddableException(message) {
    override fun getErrorEmbed(locale: DiscordLocale): MessageEmbed = getGameDataNotFoundEmbed(locale)
}

class DataConflictException(message: String = "Data Conflict!") : EmbeddableException(message) {
    override fun getErrorEmbed(locale: DiscordLocale): MessageEmbed = getDataConflictErrorEmbed(locale)
}

