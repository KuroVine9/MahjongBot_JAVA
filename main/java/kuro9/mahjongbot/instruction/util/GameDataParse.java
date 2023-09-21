package kuro9.mahjongbot.instruction.util;

import kuro9.mahjongbot.annotation.IntRange;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameDataParse {
    public static long getGuildID(GenericCommandInteractionEvent event) {
        long guildID;
        if (event.getOption("guild") == null) {
            if (event.getGuild() == null) {
                // 상호작용이 guild 내에서 이루어지던가, 파라미터에 guild 키로 된 값이 존재하여야 함.
                return 0;
            }
            else guildID = event.getGuild().getIdLong();
        }
        else guildID = Objects.requireNonNull(event.getOption("guild")).getAsLong();

        return guildID;
    }

    public static String getGameGroup(GenericCommandInteractionEvent event) {
        return ((event.getOption("game_group") == null) ?
                "" : Objects.requireNonNull(event.getOption("game_group")).getAsString());
    }

    public static long getButtonGuildID(ButtonInteractionEvent event) {
        return Objects.requireNonNull(event.getGuild()).getIdLong();
    }

    public static String getButtonGameGroup(ButtonInteractionEvent event) {
        String pattern = "key=\\d{1,4}-\\d{1,2}-\\d-[A-Z]{3}-\\d+-\\d+-([A-Za-z0-9_]{0,15})=";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(Arrays.toString(Base64.getDecoder().decode(event.getMessage().getContentDisplay().getBytes(StandardCharsets.US_ASCII))));
        if (m.find()) {
            return m.group(1);
        }
        else return "";
    }

    @IntRange(inclusiveStart = 1, inclusiveEnd = 12)
    public static int getValidMonth(GenericInteractionCreateEvent event) {
        if (event instanceof SlashCommandInteractionEvent s) {
            return ((s.getOption("month") == null) ?
                    LocalDate.now().getMonthValue() :
                    (int) Objects.requireNonNull(s.getOption("month")).getAsLong());
        }
        else if (event instanceof ButtonInteractionEvent b) {
            String pattern = "\\[\\d{4}.(\\d{2})";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(b.getMessage().getContentDisplay());
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
            else return 0;
        }
        else return 0;
    }

    public static int getValidYear(GenericInteractionCreateEvent event) {
        if (event instanceof SlashCommandInteractionEvent s) {
            return ((s.getOption("year") == null) ?
                    LocalDate.now().getYear() :
                    (int) Objects.requireNonNull(s.getOption("year")).getAsLong());
        }
        else if (event instanceof ButtonInteractionEvent b) {
            String pattern = "\\[(\\d{4})";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(b.getMessage().getContentDisplay());
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
            else return 0;
        }
        else return 0;
    }

    public static int getValidFilter(GenericInteractionCreateEvent event) {
        if (event instanceof SlashCommandInteractionEvent s) {
            return ((s.getOption("filter") == null) ?
                    0 : (int) Objects.requireNonNull(s.getOption("filter")).getAsLong());
        }
        else if (event instanceof ButtonInteractionEvent b) {
            String pattern = "\\([A-Za-z ]*(\\d+)";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(b.getMessage().getContentDisplay());
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
            else return 0;
        }
        else return 0;
    }

    @IntRange(inclusiveStart = 1, inclusiveEnd = 2)
    public int getValidSeason(GenericInteractionCreateEvent event) {
        if (event instanceof SlashCommandInteractionEvent s) {
            return ((s.getOption("season") == null) ?
                    ((LocalDateTime.now().getMonthValue() - 1) / 6) + 1 :
                    (int) Objects.requireNonNull(s.getOption("season")).getAsLong());
        }
        else if (event instanceof ButtonInteractionEvent b) {
            String pattern = "\\[\\d{4}.(\\d)";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(b.getMessage().getContentDisplay());
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
            else return 0;
        }
        else return 0;
    }
}
