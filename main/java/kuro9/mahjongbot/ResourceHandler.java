package kuro9.mahjongbot;

import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceHandler {

    final static ArrayList<DiscordLocale> supportLocale = new ArrayList<>(2) {{
        add(DiscordLocale.KOREAN);
        add(DiscordLocale.JAPANESE);
    }};

    public static ResourceBundle getResource(GenericInteractionCreateEvent event) {
        return ResourceBundle.getBundle(
                "localization/Instructions",
                supportLocale.contains(event.getUserLocale()) ? Locale.forLanguageTag(event.getUserLocale().getLocale()) : Locale.ROOT
        );
    }

    public static ResourceBundle getResource(DiscordLocale locale) {
        return ResourceBundle.getBundle(
                "localization/Instructions",
                supportLocale.contains(locale) ? Locale.forLanguageTag(locale.getLocale()) : Locale.ROOT
        );
    }
}
