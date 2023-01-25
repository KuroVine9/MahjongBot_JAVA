package kuro9.mahjongbot;

import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceHandler {
    public static ResourceBundle getResource(GenericInteractionCreateEvent event) {
        return ResourceBundle.getBundle(
                "localization/Instructions",
                Locale.forLanguageTag(event.getUserLocale().toString())
        );
    }

    public static ResourceBundle getResource(DiscordLocale locale) {
        return ResourceBundle.getBundle(
                "localization/Instructions",
                Locale.forLanguageTag(locale.toString())
        );
    }
}
