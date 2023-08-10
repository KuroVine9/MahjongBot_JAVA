package kuro9.mahjongbot.instruction.util;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.Objects;

public class GameDataParse {
    protected long getGuildID(GenericCommandInteractionEvent event) {
        long guildID;
        if (event.getOption("guild") == null) {
            if (event.getGuild() == null) {
                throw new RuntimeException("Unexpected Condition!! - guildID parse");
            }
            else guildID = event.getGuild().getIdLong();
        }
        else guildID = event.getOption("guild").getAsLong();

        return guildID;
    }

    protected String getGameGroup(GenericCommandInteractionEvent event) {
        return ((event.getOption("game_group") == null) ?
                "" : event.getOption("game_group").getAsString());
    }

    protected long getButtonGuildID(ButtonInteractionEvent event) {
        return Objects.requireNonNull(event.getGuild()).getIdLong();
    }

    protected String getButtonGameGroup(ButtonInteractionEvent event) {
        //TODO 게임그룹 파싱 구현
    }
}
